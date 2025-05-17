package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Share;
import com.fileshareappv1.myapp.repository.ShareRepository;
import com.fileshareappv1.myapp.repository.search.ShareSearchRepository;
import com.fileshareappv1.myapp.service.dto.ShareDTO;
import com.fileshareappv1.myapp.service.mapper.ShareMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fileshareappv1.myapp.domain.Share}.
 */
@Service
@Transactional
public class ShareService {

    private static final Logger LOG = LoggerFactory.getLogger(ShareService.class);

    private final ShareRepository shareRepository;

    private final ShareMapper shareMapper;

    private final ShareSearchRepository shareSearchRepository;

    public ShareService(ShareRepository shareRepository, ShareMapper shareMapper, ShareSearchRepository shareSearchRepository) {
        this.shareRepository = shareRepository;
        this.shareMapper = shareMapper;
        this.shareSearchRepository = shareSearchRepository;
    }

    /**
     * Save a share.
     *
     * @param shareDTO the entity to save.
     * @return the persisted entity.
     */
    public ShareDTO save(ShareDTO shareDTO) {
        LOG.debug("Request to save Share : {}", shareDTO);
        Share share = shareMapper.toEntity(shareDTO);
        share = shareRepository.save(share);
        shareSearchRepository.index(share);
        return shareMapper.toDto(share);
    }

    /**
     * Update a share.
     *
     * @param shareDTO the entity to save.
     * @return the persisted entity.
     */
    public ShareDTO update(ShareDTO shareDTO) {
        LOG.debug("Request to update Share : {}", shareDTO);
        Share share = shareMapper.toEntity(shareDTO);
        share = shareRepository.save(share);
        shareSearchRepository.index(share);
        return shareMapper.toDto(share);
    }

    /**
     * Partially update a share.
     *
     * @param shareDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ShareDTO> partialUpdate(ShareDTO shareDTO) {
        LOG.debug("Request to partially update Share : {}", shareDTO);

        return shareRepository
            .findById(shareDTO.getId())
            .map(existingShare -> {
                shareMapper.partialUpdate(existingShare, shareDTO);

                return existingShare;
            })
            .map(shareRepository::save)
            .map(savedShare -> {
                shareSearchRepository.index(savedShare);
                return savedShare;
            })
            .map(shareMapper::toDto);
    }

    /**
     * Get all the shares.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ShareDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Shares");
        return shareRepository.findAll(pageable).map(shareMapper::toDto);
    }

    /**
     * Get all the shares with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ShareDTO> findAllWithEagerRelationships(Pageable pageable) {
        return shareRepository.findAllWithEagerRelationships(pageable).map(shareMapper::toDto);
    }

    /**
     * Get one share by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ShareDTO> findOne(Long id) {
        LOG.debug("Request to get Share : {}", id);
        return shareRepository.findOneWithEagerRelationships(id).map(shareMapper::toDto);
    }

    /**
     * Delete the share by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Share : {}", id);
        shareRepository.deleteById(id);
        shareSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the share corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ShareDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Shares for query {}", query);
        return shareSearchRepository.search(query, pageable).map(shareMapper::toDto);
    }
}
