package com.fileshareappv1.myapp.web.rest;

import static com.fileshareappv1.myapp.domain.FavoriteAsserts.*;
import static com.fileshareappv1.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileshareappv1.myapp.IntegrationTest;
import com.fileshareappv1.myapp.domain.Favorite;
import com.fileshareappv1.myapp.repository.FavoriteRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.repository.search.FavoriteSearchRepository;
import com.fileshareappv1.myapp.service.FavoriteService;
import com.fileshareappv1.myapp.service.dto.FavoriteDTO;
import com.fileshareappv1.myapp.service.mapper.FavoriteMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link FavoriteResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FavoriteResourceIT {

    private static final Instant DEFAULT_SAVED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SAVED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/favorites";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/favorites/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private FavoriteRepository favoriteRepositoryMock;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Mock
    private FavoriteService favoriteServiceMock;

    @Autowired
    private FavoriteSearchRepository favoriteSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFavoriteMockMvc;

    private Favorite favorite;

    private Favorite insertedFavorite;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Favorite createEntity() {
        return new Favorite().savedAt(DEFAULT_SAVED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Favorite createUpdatedEntity() {
        return new Favorite().savedAt(UPDATED_SAVED_AT);
    }

    @BeforeEach
    void initTest() {
        favorite = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedFavorite != null) {
            favoriteRepository.delete(insertedFavorite);
            favoriteSearchRepository.delete(insertedFavorite);
            insertedFavorite = null;
        }
    }

    @Test
    @Transactional
    void createFavorite() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);
        var returnedFavoriteDTO = om.readValue(
            restFavoriteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FavoriteDTO.class
        );

        // Validate the Favorite in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFavorite = favoriteMapper.toEntity(returnedFavoriteDTO);
        assertFavoriteUpdatableFieldsEquals(returnedFavorite, getPersistedFavorite(returnedFavorite));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedFavorite = returnedFavorite;
    }

    @Test
    @Transactional
    void createFavoriteWithExistingId() throws Exception {
        // Create the Favorite with an existing ID
        favorite.setId(1L);
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restFavoriteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkSavedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        // set the field null
        favorite.setSavedAt(null);

        // Create the Favorite, which fails.
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        restFavoriteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllFavorites() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);

        // Get all the favoriteList
        restFavoriteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favorite.getId().intValue())))
            .andExpect(jsonPath("$.[*].savedAt").value(hasItem(DEFAULT_SAVED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFavoritesWithEagerRelationshipsIsEnabled() throws Exception {
        when(favoriteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFavoriteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(favoriteServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFavoritesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(favoriteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFavoriteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(favoriteRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFavorite() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);

        // Get the favorite
        restFavoriteMockMvc
            .perform(get(ENTITY_API_URL_ID, favorite.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(favorite.getId().intValue()))
            .andExpect(jsonPath("$.savedAt").value(DEFAULT_SAVED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingFavorite() throws Exception {
        // Get the favorite
        restFavoriteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFavorite() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        favoriteSearchRepository.save(favorite);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());

        // Update the favorite
        Favorite updatedFavorite = favoriteRepository.findById(favorite.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFavorite are not directly saved in db
        em.detach(updatedFavorite);
        updatedFavorite.savedAt(UPDATED_SAVED_AT);
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(updatedFavorite);

        restFavoriteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, favoriteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFavoriteToMatchAllProperties(updatedFavorite);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Favorite> favoriteSearchList = Streamable.of(favoriteSearchRepository.findAll()).toList();
                Favorite testFavoriteSearch = favoriteSearchList.get(searchDatabaseSizeAfter - 1);

                assertFavoriteAllPropertiesEquals(testFavoriteSearch, updatedFavorite);
            });
    }

    @Test
    @Transactional
    void putNonExistingFavorite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        favorite.setId(longCount.incrementAndGet());

        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFavoriteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, favoriteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchFavorite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        favorite.setId(longCount.incrementAndGet());

        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFavorite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        favorite.setId(longCount.incrementAndGet());

        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateFavoriteWithPatch() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favorite using partial update
        Favorite partialUpdatedFavorite = new Favorite();
        partialUpdatedFavorite.setId(favorite.getId());

        restFavoriteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFavorite.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFavorite))
            )
            .andExpect(status().isOk());

        // Validate the Favorite in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFavoriteUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFavorite, favorite), getPersistedFavorite(favorite));
    }

    @Test
    @Transactional
    void fullUpdateFavoriteWithPatch() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favorite using partial update
        Favorite partialUpdatedFavorite = new Favorite();
        partialUpdatedFavorite.setId(favorite.getId());

        partialUpdatedFavorite.savedAt(UPDATED_SAVED_AT);

        restFavoriteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFavorite.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFavorite))
            )
            .andExpect(status().isOk());

        // Validate the Favorite in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFavoriteUpdatableFieldsEquals(partialUpdatedFavorite, getPersistedFavorite(partialUpdatedFavorite));
    }

    @Test
    @Transactional
    void patchNonExistingFavorite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        favorite.setId(longCount.incrementAndGet());

        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFavoriteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, favoriteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(favoriteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFavorite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        favorite.setId(longCount.incrementAndGet());

        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(favoriteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFavorite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        favorite.setId(longCount.incrementAndGet());

        // Create the Favorite
        FavoriteDTO favoriteDTO = favoriteMapper.toDto(favorite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(favoriteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Favorite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteFavorite() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);
        favoriteRepository.save(favorite);
        favoriteSearchRepository.save(favorite);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the favorite
        restFavoriteMockMvc
            .perform(delete(ENTITY_API_URL_ID, favorite.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(favoriteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchFavorite() throws Exception {
        // Initialize the database
        insertedFavorite = favoriteRepository.saveAndFlush(favorite);
        favoriteSearchRepository.save(favorite);

        // Search the favorite
        restFavoriteMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + favorite.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favorite.getId().intValue())))
            .andExpect(jsonPath("$.[*].savedAt").value(hasItem(DEFAULT_SAVED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return favoriteRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Favorite getPersistedFavorite(Favorite favorite) {
        return favoriteRepository.findById(favorite.getId()).orElseThrow();
    }

    protected void assertPersistedFavoriteToMatchAllProperties(Favorite expectedFavorite) {
        assertFavoriteAllPropertiesEquals(expectedFavorite, getPersistedFavorite(expectedFavorite));
    }

    protected void assertPersistedFavoriteToMatchUpdatableProperties(Favorite expectedFavorite) {
        assertFavoriteAllUpdatablePropertiesEquals(expectedFavorite, getPersistedFavorite(expectedFavorite));
    }
}
