package com.fileshareappv1.myapp.web.rest;

import static com.fileshareappv1.myapp.domain.MentionAsserts.*;
import static com.fileshareappv1.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileshareappv1.myapp.IntegrationTest;
import com.fileshareappv1.myapp.domain.Mention;
import com.fileshareappv1.myapp.repository.MentionRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.repository.search.MentionSearchRepository;
import com.fileshareappv1.myapp.service.MentionService;
import com.fileshareappv1.myapp.service.dto.MentionDTO;
import com.fileshareappv1.myapp.service.mapper.MentionMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link MentionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MentionResourceIT {

    private static final String ENTITY_API_URL = "/api/mentions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/mentions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MentionRepository mentionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private MentionRepository mentionRepositoryMock;

    @Autowired
    private MentionMapper mentionMapper;

    @Mock
    private MentionService mentionServiceMock;

    @Autowired
    private MentionSearchRepository mentionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMentionMockMvc;

    private Mention mention;

    private Mention insertedMention;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Mention createEntity() {
        return new Mention();
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Mention createUpdatedEntity() {
        return new Mention();
    }

    @BeforeEach
    void initTest() {
        mention = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMention != null) {
            mentionRepository.delete(insertedMention);
            mentionSearchRepository.delete(insertedMention);
            insertedMention = null;
        }
    }

    @Test
    @Transactional
    void createMention() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);
        var returnedMentionDTO = om.readValue(
            restMentionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mentionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MentionDTO.class
        );

        // Validate the Mention in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMention = mentionMapper.toEntity(returnedMentionDTO);
        assertMentionUpdatableFieldsEquals(returnedMention, getPersistedMention(returnedMention));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedMention = returnedMention;
    }

    @Test
    @Transactional
    void createMentionWithExistingId() throws Exception {
        // Create the Mention with an existing ID
        mention.setId(1L);
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restMentionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mentionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllMentions() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);

        // Get all the mentionList
        restMentionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mention.getId().intValue())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMentionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(mentionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMentionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(mentionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMentionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(mentionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMentionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(mentionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMention() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);

        // Get the mention
        restMentionMockMvc
            .perform(get(ENTITY_API_URL_ID, mention.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(mention.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingMention() throws Exception {
        // Get the mention
        restMentionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMention() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        mentionSearchRepository.save(mention);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());

        // Update the mention
        Mention updatedMention = mentionRepository.findById(mention.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMention are not directly saved in db
        em.detach(updatedMention);
        MentionDTO mentionDTO = mentionMapper.toDto(updatedMention);

        restMentionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, mentionDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mentionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMentionToMatchAllProperties(updatedMention);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Mention> mentionSearchList = Streamable.of(mentionSearchRepository.findAll()).toList();
                Mention testMentionSearch = mentionSearchList.get(searchDatabaseSizeAfter - 1);

                assertMentionAllPropertiesEquals(testMentionSearch, updatedMention);
            });
    }

    @Test
    @Transactional
    void putNonExistingMention() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        mention.setId(longCount.incrementAndGet());

        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMentionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, mentionDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mentionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchMention() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        mention.setId(longCount.incrementAndGet());

        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMentionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(mentionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMention() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        mention.setId(longCount.incrementAndGet());

        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMentionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mentionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateMentionWithPatch() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the mention using partial update
        Mention partialUpdatedMention = new Mention();
        partialUpdatedMention.setId(mention.getId());

        restMentionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMention.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMention))
            )
            .andExpect(status().isOk());

        // Validate the Mention in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMentionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMention, mention), getPersistedMention(mention));
    }

    @Test
    @Transactional
    void fullUpdateMentionWithPatch() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the mention using partial update
        Mention partialUpdatedMention = new Mention();
        partialUpdatedMention.setId(mention.getId());

        restMentionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMention.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMention))
            )
            .andExpect(status().isOk());

        // Validate the Mention in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMentionUpdatableFieldsEquals(partialUpdatedMention, getPersistedMention(partialUpdatedMention));
    }

    @Test
    @Transactional
    void patchNonExistingMention() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        mention.setId(longCount.incrementAndGet());

        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMentionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, mentionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(mentionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMention() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        mention.setId(longCount.incrementAndGet());

        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMentionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(mentionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMention() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        mention.setId(longCount.incrementAndGet());

        // Create the Mention
        MentionDTO mentionDTO = mentionMapper.toDto(mention);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMentionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(mentionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Mention in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteMention() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);
        mentionRepository.save(mention);
        mentionSearchRepository.save(mention);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the mention
        restMentionMockMvc
            .perform(delete(ENTITY_API_URL_ID, mention.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(mentionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchMention() throws Exception {
        // Initialize the database
        insertedMention = mentionRepository.saveAndFlush(mention);
        mentionSearchRepository.save(mention);

        // Search the mention
        restMentionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + mention.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mention.getId().intValue())));
    }

    protected long getRepositoryCount() {
        return mentionRepository.count();
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

    protected Mention getPersistedMention(Mention mention) {
        return mentionRepository.findById(mention.getId()).orElseThrow();
    }

    protected void assertPersistedMentionToMatchAllProperties(Mention expectedMention) {
        assertMentionAllPropertiesEquals(expectedMention, getPersistedMention(expectedMention));
    }

    protected void assertPersistedMentionToMatchUpdatableProperties(Mention expectedMention) {
        assertMentionAllUpdatablePropertiesEquals(expectedMention, getPersistedMention(expectedMention));
    }
}
