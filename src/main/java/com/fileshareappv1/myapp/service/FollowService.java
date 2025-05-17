package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Follow;
import com.fileshareappv1.myapp.repository.FollowRepository;
import com.fileshareappv1.myapp.repository.search.FollowSearchRepository;
import com.fileshareappv1.myapp.service.dto.FollowDTO;
import com.fileshareappv1.myapp.service.mapper.FollowMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fileshareappv1.myapp.domain.Follow}.
 */
@Service
@Transactional
public class FollowService {

    private static final Logger LOG = LoggerFactory.getLogger(FollowService.class);

    private final FollowRepository followRepository;

    private final FollowMapper followMapper;

    private final FollowSearchRepository followSearchRepository;

    public FollowService(FollowRepository followRepository, FollowMapper followMapper, FollowSearchRepository followSearchRepository) {
        this.followRepository = followRepository;
        this.followMapper = followMapper;
        this.followSearchRepository = followSearchRepository;
    }

    /**
     * Save a follow.
     *
     * @param followDTO the entity to save.
     * @return the persisted entity.
     */
    public FollowDTO save(FollowDTO followDTO) {
        LOG.debug("Request to save Follow : {}", followDTO);
        Follow follow = followMapper.toEntity(followDTO);
        follow = followRepository.save(follow);
        followSearchRepository.index(follow);
        return followMapper.toDto(follow);
    }

    /**
     * Update a follow.
     *
     * @param followDTO the entity to save.
     * @return the persisted entity.
     */
    public FollowDTO update(FollowDTO followDTO) {
        LOG.debug("Request to update Follow : {}", followDTO);
        Follow follow = followMapper.toEntity(followDTO);
        follow = followRepository.save(follow);
        followSearchRepository.index(follow);
        return followMapper.toDto(follow);
    }

    /**
     * Partially update a follow.
     *
     * @param followDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<FollowDTO> partialUpdate(FollowDTO followDTO) {
        LOG.debug("Request to partially update Follow : {}", followDTO);

        return followRepository
            .findById(followDTO.getId())
            .map(existingFollow -> {
                followMapper.partialUpdate(existingFollow, followDTO);

                return existingFollow;
            })
            .map(followRepository::save)
            .map(savedFollow -> {
                followSearchRepository.index(savedFollow);
                return savedFollow;
            })
            .map(followMapper::toDto);
    }

    /**
     * Get all the follows.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<FollowDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Follows");
        return followRepository.findAll(pageable).map(followMapper::toDto);
    }

    /**
     * Get all the follows with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<FollowDTO> findAllWithEagerRelationships(Pageable pageable) {
        return followRepository.findAllWithEagerRelationships(pageable).map(followMapper::toDto);
    }

    /**
     * Get one follow by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<FollowDTO> findOne(Long id) {
        LOG.debug("Request to get Follow : {}", id);
        return followRepository.findOneWithEagerRelationships(id).map(followMapper::toDto);
    }

    /**
     * Delete the follow by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Follow : {}", id);
        followRepository.deleteById(id);
        followSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the follow corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<FollowDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Follows for query {}", query);
        return followSearchRepository.search(query, pageable).map(followMapper::toDto);
    }
}
