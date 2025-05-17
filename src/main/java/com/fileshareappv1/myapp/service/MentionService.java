package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Mention;
import com.fileshareappv1.myapp.repository.MentionRepository;
import com.fileshareappv1.myapp.repository.search.MentionSearchRepository;
import com.fileshareappv1.myapp.service.dto.MentionDTO;
import com.fileshareappv1.myapp.service.mapper.MentionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fileshareappv1.myapp.domain.Mention}.
 */
@Service
@Transactional
public class MentionService {

    private static final Logger LOG = LoggerFactory.getLogger(MentionService.class);

    private final MentionRepository mentionRepository;

    private final MentionMapper mentionMapper;

    private final MentionSearchRepository mentionSearchRepository;

    public MentionService(
        MentionRepository mentionRepository,
        MentionMapper mentionMapper,
        MentionSearchRepository mentionSearchRepository
    ) {
        this.mentionRepository = mentionRepository;
        this.mentionMapper = mentionMapper;
        this.mentionSearchRepository = mentionSearchRepository;
    }

    /**
     * Save a mention.
     *
     * @param mentionDTO the entity to save.
     * @return the persisted entity.
     */
    public MentionDTO save(MentionDTO mentionDTO) {
        LOG.debug("Request to save Mention : {}", mentionDTO);
        Mention mention = mentionMapper.toEntity(mentionDTO);
        mention = mentionRepository.save(mention);
        mentionSearchRepository.index(mention);
        return mentionMapper.toDto(mention);
    }

    /**
     * Update a mention.
     *
     * @param mentionDTO the entity to save.
     * @return the persisted entity.
     */
    public MentionDTO update(MentionDTO mentionDTO) {
        LOG.debug("Request to update Mention : {}", mentionDTO);
        Mention mention = mentionMapper.toEntity(mentionDTO);
        mention = mentionRepository.save(mention);
        mentionSearchRepository.index(mention);
        return mentionMapper.toDto(mention);
    }

    /**
     * Partially update a mention.
     *
     * @param mentionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MentionDTO> partialUpdate(MentionDTO mentionDTO) {
        LOG.debug("Request to partially update Mention : {}", mentionDTO);

        return mentionRepository
            .findById(mentionDTO.getId())
            .map(existingMention -> {
                mentionMapper.partialUpdate(existingMention, mentionDTO);

                return existingMention;
            })
            .map(mentionRepository::save)
            .map(savedMention -> {
                mentionSearchRepository.index(savedMention);
                return savedMention;
            })
            .map(mentionMapper::toDto);
    }

    /**
     * Get all the mentions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MentionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Mentions");
        return mentionRepository.findAll(pageable).map(mentionMapper::toDto);
    }

    /**
     * Get all the mentions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<MentionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return mentionRepository.findAllWithEagerRelationships(pageable).map(mentionMapper::toDto);
    }

    /**
     * Get one mention by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MentionDTO> findOne(Long id) {
        LOG.debug("Request to get Mention : {}", id);
        return mentionRepository.findOneWithEagerRelationships(id).map(mentionMapper::toDto);
    }

    /**
     * Delete the mention by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Mention : {}", id);
        mentionRepository.deleteById(id);
        mentionSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the mention corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MentionDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Mentions for query {}", query);
        return mentionSearchRepository.search(query, pageable).map(mentionMapper::toDto);
    }
}
