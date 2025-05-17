package com.fileshareappv1.myapp.web.rest;

import static com.fileshareappv1.myapp.domain.ReactionAsserts.*;
import static com.fileshareappv1.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileshareappv1.myapp.IntegrationTest;
import com.fileshareappv1.myapp.domain.Reaction;
import com.fileshareappv1.myapp.domain.enumeration.ReactionType;
import com.fileshareappv1.myapp.repository.ReactionRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.repository.search.ReactionSearchRepository;
import com.fileshareappv1.myapp.service.ReactionService;
import com.fileshareappv1.myapp.service.dto.ReactionDTO;
import com.fileshareappv1.myapp.service.mapper.ReactionMapper;
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
 * Integration tests for the {@link ReactionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ReactionResourceIT {

    private static final ReactionType DEFAULT_TYPE = ReactionType.LIKE;
    private static final ReactionType UPDATED_TYPE = ReactionType.LOVE;

    private static final Instant DEFAULT_REACTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REACTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/reactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/reactions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ReactionRepository reactionRepositoryMock;

    @Autowired
    private ReactionMapper reactionMapper;

    @Mock
    private ReactionService reactionServiceMock;

    @Autowired
    private ReactionSearchRepository reactionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReactionMockMvc;

    private Reaction reaction;

    private Reaction insertedReaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reaction createEntity() {
        return new Reaction().type(DEFAULT_TYPE).reactedAt(DEFAULT_REACTED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reaction createUpdatedEntity() {
        return new Reaction().type(UPDATED_TYPE).reactedAt(UPDATED_REACTED_AT);
    }

    @BeforeEach
    void initTest() {
        reaction = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedReaction != null) {
            reactionRepository.delete(insertedReaction);
            reactionSearchRepository.delete(insertedReaction);
            insertedReaction = null;
        }
    }

    @Test
    @Transactional
    void createReaction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);
        var returnedReactionDTO = om.readValue(
            restReactionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReactionDTO.class
        );

        // Validate the Reaction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReaction = reactionMapper.toEntity(returnedReactionDTO);
        assertReactionUpdatableFieldsEquals(returnedReaction, getPersistedReaction(returnedReaction));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedReaction = returnedReaction;
    }

    @Test
    @Transactional
    void createReactionWithExistingId() throws Exception {
        // Create the Reaction with an existing ID
        reaction.setId(1L);
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restReactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        // set the field null
        reaction.setType(null);

        // Create the Reaction, which fails.
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        restReactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkReactedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        // set the field null
        reaction.setReactedAt(null);

        // Create the Reaction, which fails.
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        restReactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllReactions() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList
        restReactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].reactedAt").value(hasItem(DEFAULT_REACTED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReactionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(reactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(reactionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReactionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(reactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(reactionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get the reaction
        restReactionMockMvc
            .perform(get(ENTITY_API_URL_ID, reaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reaction.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.reactedAt").value(DEFAULT_REACTED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingReaction() throws Exception {
        // Get the reaction
        restReactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactionSearchRepository.save(reaction);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());

        // Update the reaction
        Reaction updatedReaction = reactionRepository.findById(reaction.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReaction are not directly saved in db
        em.detach(updatedReaction);
        updatedReaction.type(UPDATED_TYPE).reactedAt(UPDATED_REACTED_AT);
        ReactionDTO reactionDTO = reactionMapper.toDto(updatedReaction);

        restReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReactionToMatchAllProperties(updatedReaction);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Reaction> reactionSearchList = Streamable.of(reactionSearchRepository.findAll()).toList();
                Reaction testReactionSearch = reactionSearchList.get(searchDatabaseSizeAfter - 1);

                assertReactionAllPropertiesEquals(testReactionSearch, updatedReaction);
            });
    }

    @Test
    @Transactional
    void putNonExistingReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        reaction.setId(longCount.incrementAndGet());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        reaction.setId(longCount.incrementAndGet());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        reaction.setId(longCount.incrementAndGet());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateReactionWithPatch() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reaction using partial update
        Reaction partialUpdatedReaction = new Reaction();
        partialUpdatedReaction.setId(reaction.getId());

        partialUpdatedReaction.type(UPDATED_TYPE);

        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReaction))
            )
            .andExpect(status().isOk());

        // Validate the Reaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReactionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedReaction, reaction), getPersistedReaction(reaction));
    }

    @Test
    @Transactional
    void fullUpdateReactionWithPatch() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reaction using partial update
        Reaction partialUpdatedReaction = new Reaction();
        partialUpdatedReaction.setId(reaction.getId());

        partialUpdatedReaction.type(UPDATED_TYPE).reactedAt(UPDATED_REACTED_AT);

        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReaction))
            )
            .andExpect(status().isOk());

        // Validate the Reaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReactionUpdatableFieldsEquals(partialUpdatedReaction, getPersistedReaction(partialUpdatedReaction));
    }

    @Test
    @Transactional
    void patchNonExistingReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        reaction.setId(longCount.incrementAndGet());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reactionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        reaction.setId(longCount.incrementAndGet());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        reaction.setId(longCount.incrementAndGet());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);
        reactionRepository.save(reaction);
        reactionSearchRepository.save(reaction);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the reaction
        restReactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, reaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(reactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);
        reactionSearchRepository.save(reaction);

        // Search the reaction
        restReactionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + reaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].reactedAt").value(hasItem(DEFAULT_REACTED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return reactionRepository.count();
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

    protected Reaction getPersistedReaction(Reaction reaction) {
        return reactionRepository.findById(reaction.getId()).orElseThrow();
    }

    protected void assertPersistedReactionToMatchAllProperties(Reaction expectedReaction) {
        assertReactionAllPropertiesEquals(expectedReaction, getPersistedReaction(expectedReaction));
    }

    protected void assertPersistedReactionToMatchUpdatableProperties(Reaction expectedReaction) {
        assertReactionAllUpdatablePropertiesEquals(expectedReaction, getPersistedReaction(expectedReaction));
    }
}
