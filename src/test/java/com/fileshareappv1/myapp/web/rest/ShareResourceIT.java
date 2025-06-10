package com.fileshareappv1.myapp.web.rest;

import static com.fileshareappv1.myapp.domain.ShareAsserts.*;
import static com.fileshareappv1.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileshareappv1.myapp.IntegrationTest;
import com.fileshareappv1.myapp.domain.Share;
import com.fileshareappv1.myapp.repository.ShareRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.repository.search.ShareSearchRepository;
import com.fileshareappv1.myapp.service.ShareService;
import com.fileshareappv1.myapp.service.dto.ShareDTO;
import com.fileshareappv1.myapp.service.mapper.ShareMapper;
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
 * Integration tests for the {@link ShareResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ShareResourceIT {

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/shares";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/shares/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ShareRepository shareRepositoryMock;

    @Autowired
    private ShareMapper shareMapper;

    @Mock
    private ShareService shareServiceMock;

    @Autowired
    private ShareSearchRepository shareSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restShareMockMvc;

    private Share share;

    private Share insertedShare;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Share createEntity() {
        return new Share().createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Share createUpdatedEntity() {
        return new Share().createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        share = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedShare != null) {
            shareRepository.delete(insertedShare);
            shareSearchRepository.delete(insertedShare);
            insertedShare = null;
        }
    }

    @Test
    @Transactional
    void createShare() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);
        var returnedShareDTO = om.readValue(
            restShareMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shareDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ShareDTO.class
        );

        // Validate the Share in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedShare = shareMapper.toEntity(returnedShareDTO);
        assertShareUpdatableFieldsEquals(returnedShare, getPersistedShare(returnedShare));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedShare = returnedShare;
    }

    @Test
    @Transactional
    void createShareWithExistingId() throws Exception {
        // Create the Share with an existing ID
        share.setId(1L);
        ShareDTO shareDTO = shareMapper.toDto(share);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restShareMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shareDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        // set the field null
        share.setCreatedAt(null);

        // Create the Share, which fails.
        ShareDTO shareDTO = shareMapper.toDto(share);

        restShareMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shareDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllShares() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);

        // Get all the shareList
        restShareMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(share.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSharesWithEagerRelationshipsIsEnabled() throws Exception {
        when(shareServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restShareMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(shareServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSharesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(shareServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restShareMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(shareRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getShare() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);

        // Get the share
        restShareMockMvc
            .perform(get(ENTITY_API_URL_ID, share.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(share.getId().intValue()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingShare() throws Exception {
        // Get the share
        restShareMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingShare() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        shareSearchRepository.save(share);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());

        // Update the share
        Share updatedShare = shareRepository.findById(share.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedShare are not directly saved in db
        em.detach(updatedShare);
        updatedShare.createdAt(UPDATED_CREATED_AT);
        ShareDTO shareDTO = shareMapper.toDto(updatedShare);

        restShareMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shareDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shareDTO))
            )
            .andExpect(status().isOk());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedShareToMatchAllProperties(updatedShare);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Share> shareSearchList = Streamable.of(shareSearchRepository.findAll()).toList();
                Share testShareSearch = shareSearchList.get(searchDatabaseSizeAfter - 1);

                assertShareAllPropertiesEquals(testShareSearch, updatedShare);
            });
    }

    @Test
    @Transactional
    void putNonExistingShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        share.setId(longCount.incrementAndGet());

        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShareMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shareDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        share.setId(longCount.incrementAndGet());

        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShareMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        share.setId(longCount.incrementAndGet());

        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShareMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shareDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateShareWithPatch() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the share using partial update
        Share partialUpdatedShare = new Share();
        partialUpdatedShare.setId(share.getId());

        partialUpdatedShare.createdAt(UPDATED_CREATED_AT);

        restShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShare.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShare))
            )
            .andExpect(status().isOk());

        // Validate the Share in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShareUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedShare, share), getPersistedShare(share));
    }

    @Test
    @Transactional
    void fullUpdateShareWithPatch() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the share using partial update
        Share partialUpdatedShare = new Share();
        partialUpdatedShare.setId(share.getId());

        partialUpdatedShare.createdAt(UPDATED_CREATED_AT);

        restShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShare.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShare))
            )
            .andExpect(status().isOk());

        // Validate the Share in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertShareUpdatableFieldsEquals(partialUpdatedShare, getPersistedShare(partialUpdatedShare));
    }

    @Test
    @Transactional
    void patchNonExistingShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        share.setId(longCount.incrementAndGet());

        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shareDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        share.setId(longCount.incrementAndGet());

        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        share.setId(longCount.incrementAndGet());

        // Create the Share
        ShareDTO shareDTO = shareMapper.toDto(share);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShareMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(shareDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Share in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteShare() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);
        shareRepository.save(share);
        shareSearchRepository.save(share);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the share
        restShareMockMvc
            .perform(delete(ENTITY_API_URL_ID, share.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(shareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchShare() throws Exception {
        // Initialize the database
        insertedShare = shareRepository.saveAndFlush(share);
        shareSearchRepository.save(share);

        // Search the share
        restShareMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + share.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(share.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return shareRepository.count();
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

    protected Share getPersistedShare(Share share) {
        return shareRepository.findById(share.getId()).orElseThrow();
    }

    protected void assertPersistedShareToMatchAllProperties(Share expectedShare) {
        assertShareAllPropertiesEquals(expectedShare, getPersistedShare(expectedShare));
    }

    protected void assertPersistedShareToMatchUpdatableProperties(Share expectedShare) {
        assertShareAllUpdatablePropertiesEquals(expectedShare, getPersistedShare(expectedShare));
    }
}
