package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.repository.FavoriteRepository;
import com.fileshareappv1.myapp.service.FavoriteService;
import com.fileshareappv1.myapp.service.dto.FavoriteDTO;
import com.fileshareappv1.myapp.web.rest.errors.BadRequestAlertException;
import com.fileshareappv1.myapp.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.fileshareappv1.myapp.domain.Favorite}.
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteResource {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteResource.class);

    private static final String ENTITY_NAME = "favorite";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FavoriteService favoriteService;

    private final FavoriteRepository favoriteRepository;

    public FavoriteResource(FavoriteService favoriteService, FavoriteRepository favoriteRepository) {
        this.favoriteService = favoriteService;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * {@code POST  /favorites} : Create a new favorite.
     *
     * @param favoriteDTO the favoriteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new favoriteDTO, or with status {@code 400 (Bad Request)} if the favorite has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FavoriteDTO> createFavorite(@Valid @RequestBody FavoriteDTO favoriteDTO) throws URISyntaxException {
        LOG.debug("REST request to save Favorite : {}", favoriteDTO);
        if (favoriteDTO.getId() != null) {
            throw new BadRequestAlertException("A new favorite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        favoriteDTO = favoriteService.save(favoriteDTO);
        return ResponseEntity.created(new URI("/api/favorites/" + favoriteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, favoriteDTO.getId().toString()))
            .body(favoriteDTO);
    }

    /**
     * {@code PUT  /favorites/:id} : Updates an existing favorite.
     *
     * @param id the id of the favoriteDTO to save.
     * @param favoriteDTO the favoriteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favoriteDTO,
     * or with status {@code 400 (Bad Request)} if the favoriteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the favoriteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FavoriteDTO> updateFavorite(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FavoriteDTO favoriteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Favorite : {}, {}", id, favoriteDTO);
        if (favoriteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, favoriteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!favoriteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        favoriteDTO = favoriteService.update(favoriteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, favoriteDTO.getId().toString()))
            .body(favoriteDTO);
    }

    /**
     * {@code PATCH  /favorites/:id} : Partial updates given fields of an existing favorite, field will ignore if it is null
     *
     * @param id the id of the favoriteDTO to save.
     * @param favoriteDTO the favoriteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favoriteDTO,
     * or with status {@code 400 (Bad Request)} if the favoriteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the favoriteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the favoriteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FavoriteDTO> partialUpdateFavorite(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FavoriteDTO favoriteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Favorite partially : {}, {}", id, favoriteDTO);
        if (favoriteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, favoriteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!favoriteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FavoriteDTO> result = favoriteService.partialUpdate(favoriteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, favoriteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /favorites} : get all the favorites.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of favorites in body.
     */
    @GetMapping("")
    public List<FavoriteDTO> getAllFavorites(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Favorites");
        return favoriteService.findAll();
    }

    /**
     * {@code GET  /favorites/:id} : get the "id" favorite.
     *
     * @param id the id of the favoriteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the favoriteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FavoriteDTO> getFavorite(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Favorite : {}", id);
        Optional<FavoriteDTO> favoriteDTO = favoriteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(favoriteDTO);
    }

    /**
     * {@code DELETE  /favorites/:id} : delete the "id" favorite.
     *
     * @param id the id of the favoriteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Favorite : {}", id);
        favoriteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /favorites/_search?query=:query} : search for the favorite corresponding
     * to the query.
     *
     * @param query the query of the favorite search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<FavoriteDTO> searchFavorites(@RequestParam("query") String query) {
        LOG.debug("REST request to search Favorites for query {}", query);
        try {
            return favoriteService.search(query);
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }

    /**
     * {@code GET  /favorites/by-current-user} : get all the favorites by current user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of favorites in body.
     */
    @GetMapping("/by-current-user")
    public ResponseEntity<List<FavoriteDTO>> getAllFavoritesByCurrentUser(Pageable pageable) {
        LOG.debug("REST request to get all Favorites by current user");
        return ResponseEntity.ok().body(favoriteService.findAllByCurrentUser(pageable).getContent());
    }
}
