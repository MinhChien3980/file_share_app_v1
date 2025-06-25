package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.repository.MentionRepository;
import com.fileshareappv1.myapp.service.MentionService;
import com.fileshareappv1.myapp.service.dto.MentionDTO;
import com.fileshareappv1.myapp.web.rest.errors.BadRequestAlertException;
import com.fileshareappv1.myapp.web.rest.errors.ElasticsearchExceptionMapper;
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
 * REST controller for managing {@link com.fileshareappv1.myapp.domain.Mention}.
 */
@RestController
@RequestMapping("/api/mentions")
public class MentionResource {

    private static final Logger LOG = LoggerFactory.getLogger(MentionResource.class);

    private static final String ENTITY_NAME = "mention";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MentionService mentionService;

    private final MentionRepository mentionRepository;

    public MentionResource(MentionService mentionService, MentionRepository mentionRepository) {
        this.mentionService = mentionService;
        this.mentionRepository = mentionRepository;
    }

    /**
     * {@code POST  /mentions} : Create a new mention.
     *
     * @param mentionDTO the mentionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new mentionDTO, or with status {@code 400 (Bad Request)} if the mention has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MentionDTO> createMention(@RequestBody MentionDTO mentionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Mention : {}", mentionDTO);
        if (mentionDTO.getId() != null) {
            throw new BadRequestAlertException("A new mention cannot already have an ID", ENTITY_NAME, "idexists");
        }
        mentionDTO = mentionService.save(mentionDTO);
        return ResponseEntity.created(new URI("/api/mentions/" + mentionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, mentionDTO.getId().toString()))
            .body(mentionDTO);
    }

    /**
     * {@code PUT  /mentions/:id} : Updates an existing mention.
     *
     * @param id the id of the mentionDTO to save.
     * @param mentionDTO the mentionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated mentionDTO,
     * or with status {@code 400 (Bad Request)} if the mentionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the mentionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MentionDTO> updateMention(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MentionDTO mentionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Mention : {}, {}", id, mentionDTO);
        if (mentionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, mentionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!mentionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        mentionDTO = mentionService.update(mentionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, mentionDTO.getId().toString()))
            .body(mentionDTO);
    }

    /**
     * {@code PATCH  /mentions/:id} : Partial updates given fields of an existing mention, field will ignore if it is null
     *
     * @param id the id of the mentionDTO to save.
     * @param mentionDTO the mentionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated mentionDTO,
     * or with status {@code 400 (Bad Request)} if the mentionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the mentionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the mentionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MentionDTO> partialUpdateMention(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MentionDTO mentionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Mention partially : {}, {}", id, mentionDTO);
        if (mentionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, mentionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!mentionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MentionDTO> result = mentionService.partialUpdate(mentionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, mentionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /mentions} : get all the mentions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of mentions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MentionDTO>> getAllMentions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Mentions");
        Page<MentionDTO> page;
        if (eagerload) {
            page = mentionService.findAllWithEagerRelationships(pageable);
        } else {
            page = mentionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /mentions/:id} : get the "id" mention.
     *
     * @param id the id of the mentionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the mentionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MentionDTO> getMention(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Mention : {}", id);
        Optional<MentionDTO> mentionDTO = mentionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(mentionDTO);
    }

    /**
     * {@code DELETE  /mentions/:id} : delete the "id" mention.
     *
     * @param id the id of the mentionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMention(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Mention : {}", id);
        mentionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /mentions/_search?query=:query} : search for the mention corresponding
     * to the query.
     *
     * @param query the query of the mention search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<MentionDTO>> searchMentions(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Mentions for query {}", query);
        try {
            Page<MentionDTO> page = mentionService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
