package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Favorite;
import com.fileshareappv1.myapp.repository.FavoriteRepository;
import com.fileshareappv1.myapp.repository.search.FavoriteSearchRepository;
import com.fileshareappv1.myapp.service.dto.FavoriteDTO;
import com.fileshareappv1.myapp.service.mapper.FavoriteMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fileshareappv1.myapp.domain.Favorite}.
 */
@Service
@Transactional
public class FavoriteService {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;

    private final FavoriteMapper favoriteMapper;

    private final FavoriteSearchRepository favoriteSearchRepository;

    public FavoriteService(
        FavoriteRepository favoriteRepository,
        FavoriteMapper favoriteMapper,
        FavoriteSearchRepository favoriteSearchRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.favoriteMapper = favoriteMapper;
        this.favoriteSearchRepository = favoriteSearchRepository;
    }

    /**
     * Save a favorite.
     *
     * @param favoriteDTO the entity to save.
     * @return the persisted entity.
     */
    public FavoriteDTO save(FavoriteDTO favoriteDTO) {
        LOG.debug("Request to save Favorite : {}", favoriteDTO);
        Favorite favorite = favoriteMapper.toEntity(favoriteDTO);
        favorite = favoriteRepository.save(favorite);
        favoriteSearchRepository.index(favorite);
        return favoriteMapper.toDto(favorite);
    }

    /**
     * Update a favorite.
     *
     * @param favoriteDTO the entity to save.
     * @return the persisted entity.
     */
    public FavoriteDTO update(FavoriteDTO favoriteDTO) {
        LOG.debug("Request to update Favorite : {}", favoriteDTO);
        Favorite favorite = favoriteMapper.toEntity(favoriteDTO);
        favorite = favoriteRepository.save(favorite);
        favoriteSearchRepository.index(favorite);
        return favoriteMapper.toDto(favorite);
    }

    /**
     * Partially update a favorite.
     *
     * @param favoriteDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<FavoriteDTO> partialUpdate(FavoriteDTO favoriteDTO) {
        LOG.debug("Request to partially update Favorite : {}", favoriteDTO);

        return favoriteRepository
            .findById(favoriteDTO.getId())
            .map(existingFavorite -> {
                favoriteMapper.partialUpdate(existingFavorite, favoriteDTO);

                return existingFavorite;
            })
            .map(favoriteRepository::save)
            .map(savedFavorite -> {
                favoriteSearchRepository.index(savedFavorite);
                return savedFavorite;
            })
            .map(favoriteMapper::toDto);
    }

    /**
     * Get all the favorites.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<FavoriteDTO> findAll() {
        LOG.debug("Request to get all Favorites");
        return favoriteRepository.findAll().stream().map(favoriteMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the favorites with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<FavoriteDTO> findAllWithEagerRelationships(Pageable pageable) {
        return favoriteRepository.findAllWithEagerRelationships(pageable).map(favoriteMapper::toDto);
    }

    /**
     * Get one favorite by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<FavoriteDTO> findOne(Long id) {
        LOG.debug("Request to get Favorite : {}", id);
        return favoriteRepository.findOneWithEagerRelationships(id).map(favoriteMapper::toDto);
    }

    /**
     * Delete the favorite by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Favorite : {}", id);
        favoriteRepository.deleteById(id);
        favoriteSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the favorite corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<FavoriteDTO> search(String query) {
        LOG.debug("Request to search Favorites for query {}", query);
        try {
            return StreamSupport.stream(favoriteSearchRepository.search(query).spliterator(), false).map(favoriteMapper::toDto).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
