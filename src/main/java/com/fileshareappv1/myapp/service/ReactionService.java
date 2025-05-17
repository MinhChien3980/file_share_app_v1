package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Reaction;
import com.fileshareappv1.myapp.repository.ReactionRepository;
import com.fileshareappv1.myapp.repository.search.ReactionSearchRepository;
import com.fileshareappv1.myapp.service.dto.ReactionDTO;
import com.fileshareappv1.myapp.service.mapper.ReactionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fileshareappv1.myapp.domain.Reaction}.
 */
@Service
@Transactional
public class ReactionService {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionService.class);

    private final ReactionRepository reactionRepository;

    private final ReactionMapper reactionMapper;

    private final ReactionSearchRepository reactionSearchRepository;

    public ReactionService(
        ReactionRepository reactionRepository,
        ReactionMapper reactionMapper,
        ReactionSearchRepository reactionSearchRepository
    ) {
        this.reactionRepository = reactionRepository;
        this.reactionMapper = reactionMapper;
        this.reactionSearchRepository = reactionSearchRepository;
    }

    /**
     * Save a reaction.
     *
     * @param reactionDTO the entity to save.
     * @return the persisted entity.
     */
    public ReactionDTO save(ReactionDTO reactionDTO) {
        LOG.debug("Request to save Reaction : {}", reactionDTO);
        Reaction reaction = reactionMapper.toEntity(reactionDTO);
        reaction = reactionRepository.save(reaction);
        reactionSearchRepository.index(reaction);
        return reactionMapper.toDto(reaction);
    }

    /**
     * Update a reaction.
     *
     * @param reactionDTO the entity to save.
     * @return the persisted entity.
     */
    public ReactionDTO update(ReactionDTO reactionDTO) {
        LOG.debug("Request to update Reaction : {}", reactionDTO);
        Reaction reaction = reactionMapper.toEntity(reactionDTO);
        reaction = reactionRepository.save(reaction);
        reactionSearchRepository.index(reaction);
        return reactionMapper.toDto(reaction);
    }

    /**
     * Partially update a reaction.
     *
     * @param reactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReactionDTO> partialUpdate(ReactionDTO reactionDTO) {
        LOG.debug("Request to partially update Reaction : {}", reactionDTO);

        return reactionRepository
            .findById(reactionDTO.getId())
            .map(existingReaction -> {
                reactionMapper.partialUpdate(existingReaction, reactionDTO);

                return existingReaction;
            })
            .map(reactionRepository::save)
            .map(savedReaction -> {
                reactionSearchRepository.index(savedReaction);
                return savedReaction;
            })
            .map(reactionMapper::toDto);
    }

    /**
     * Get all the reactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ReactionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Reactions");
        return reactionRepository.findAll(pageable).map(reactionMapper::toDto);
    }

    /**
     * Get all the reactions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ReactionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return reactionRepository.findAllWithEagerRelationships(pageable).map(reactionMapper::toDto);
    }

    /**
     * Get one reaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReactionDTO> findOne(Long id) {
        LOG.debug("Request to get Reaction : {}", id);
        return reactionRepository.findOneWithEagerRelationships(id).map(reactionMapper::toDto);
    }

    /**
     * Delete the reaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Reaction : {}", id);
        reactionRepository.deleteById(id);
        reactionSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the reaction corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ReactionDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Reactions for query {}", query);
        return reactionSearchRepository.search(query, pageable).map(reactionMapper::toDto);
    }
}
