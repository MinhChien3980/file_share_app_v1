package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.repository.ShareRepository;
import com.fileshareappv1.myapp.service.ShareService;
import com.fileshareappv1.myapp.service.dto.ShareDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.fileshareappv1.myapp.domain.Share}.
 */
@RestController
@RequestMapping("/api/shares")
public class ShareResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShareResource.class);

    private static final String ENTITY_NAME = "share";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShareService shareService;

    private final ShareRepository shareRepository;

    public ShareResource(ShareService shareService, ShareRepository shareRepository) {
        this.shareService = shareService;
        this.shareRepository = shareRepository;
    }

    /**
     * {@code POST  /shares} : Create a new share.
     *
     * @param shareDTO the shareDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shareDTO, or with status {@code 400 (Bad Request)} if the share has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ShareDTO> createShare(@Valid @RequestBody ShareDTO shareDTO) throws URISyntaxException {
        LOG.debug("REST request to save Share : {}", shareDTO);
        if (shareDTO.getId() != null) {
            throw new BadRequestAlertException("A new share cannot already have an ID", ENTITY_NAME, "idexists");
        }
        shareDTO = shareService.save(shareDTO);
        return ResponseEntity.created(new URI("/api/shares/" + shareDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, shareDTO.getId().toString()))
            .body(shareDTO);
    }

    /**
     * {@code PUT  /shares/:id} : Updates an existing share.
     *
     * @param id the id of the shareDTO to save.
     * @param shareDTO the shareDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shareDTO,
     * or with status {@code 400 (Bad Request)} if the shareDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shareDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShareDTO> updateShare(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ShareDTO shareDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Share : {}, {}", id, shareDTO);
        if (shareDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shareDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shareRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        shareDTO = shareService.update(shareDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shareDTO.getId().toString()))
            .body(shareDTO);
    }

    /**
     * {@code PATCH  /shares/:id} : Partial updates given fields of an existing share, field will ignore if it is null
     *
     * @param id the id of the shareDTO to save.
     * @param shareDTO the shareDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shareDTO,
     * or with status {@code 400 (Bad Request)} if the shareDTO is not valid,
     * or with status {@code 404 (Not Found)} if the shareDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the shareDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ShareDTO> partialUpdateShare(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ShareDTO shareDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Share partially : {}, {}", id, shareDTO);
        if (shareDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shareDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shareRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ShareDTO> result = shareService.partialUpdate(shareDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shareDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /shares} : get all the shares.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shares in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ShareDTO>> getAllShares(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Shares");
        Page<ShareDTO> page;
        if (eagerload) {
            page = shareService.findAllWithEagerRelationships(pageable);
        } else {
            page = shareService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /shares/:id} : get the "id" share.
     *
     * @param id the id of the shareDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shareDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShareDTO> getShare(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Share : {}", id);
        Optional<ShareDTO> shareDTO = shareService.findOne(id);
        return ResponseUtil.wrapOrNotFound(shareDTO);
    }

    /**
     * {@code DELETE  /shares/:id} : delete the "id" share.
     *
     * @param id the id of the shareDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Share : {}", id);
        shareService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /shares/_search?query=:query} : search for the share corresponding
     * to the query.
     *
     * @param query the query of the share search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<ShareDTO>> searchShares(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Shares for query {}", query);
        try {
            Page<ShareDTO> page = shareService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
