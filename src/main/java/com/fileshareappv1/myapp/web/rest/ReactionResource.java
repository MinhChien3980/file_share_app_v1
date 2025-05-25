package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.repository.ReactionRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.security.SecurityUtils;
import com.fileshareappv1.myapp.service.ReactionService;
import com.fileshareappv1.myapp.service.dto.ReactionDTO;
import com.fileshareappv1.myapp.web.rest.errors.BadRequestAlertException;
import com.fileshareappv1.myapp.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
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
 * REST controller for managing {@link com.fileshareappv1.myapp.domain.Reaction}.
 */
@RestController
@RequestMapping("/api/reactions")
public class ReactionResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionResource.class);

    private static final String ENTITY_NAME = "reaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReactionService reactionService;

    private final ReactionRepository reactionRepository;

    private final UserRepository userRepository;

    public ReactionResource(ReactionService reactionService, ReactionRepository reactionRepository, UserRepository userRepository) {
        this.reactionService = reactionService;
        this.reactionRepository = reactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST  /reactions} : Create a new reaction.
     *
     * @param reactionDTO the reactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reactionDTO, or with status {@code 400 (Bad Request)} if the reaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReactionDTO> createReaction(@Valid @RequestBody ReactionDTO reactionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Reaction : {}", reactionDTO);
        if (reactionDTO.getId() != null) {
            throw new BadRequestAlertException("A new reaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        reactionDTO = reactionService.save(reactionDTO);
        return ResponseEntity.created(new URI("/api/reactions/" + reactionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, reactionDTO.getId().toString()))
            .body(reactionDTO);
    }

    /**
     * {@code PUT  /reactions/:id} : Updates an existing reaction.
     *
     * @param id the id of the reactionDTO to save.
     * @param reactionDTO the reactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reactionDTO,
     * or with status {@code 400 (Bad Request)} if the reactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReactionDTO> updateReaction(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReactionDTO reactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reaction : {}, {}", id, reactionDTO);
        if (reactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        reactionDTO = reactionService.update(reactionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reactionDTO.getId().toString()))
            .body(reactionDTO);
    }

    /**
     * {@code PATCH  /reactions/:id} : Partial updates given fields of an existing reaction, field will ignore if it is null
     *
     * @param id the id of the reactionDTO to save.
     * @param reactionDTO the reactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reactionDTO,
     * or with status {@code 400 (Bad Request)} if the reactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReactionDTO> partialUpdateReaction(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReactionDTO reactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reaction partially : {}, {}", id, reactionDTO);
        if (reactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReactionDTO> result = reactionService.partialUpdate(reactionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reactionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reactions} : get all the reactions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reactions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReactionDTO>> getAllReactions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Reactions");
        Page<ReactionDTO> page;
        if (eagerload) {
            page = reactionService.findAllWithEagerRelationships(pageable);
        } else {
            page = reactionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reactions/:id} : get the "id" reaction.
     *
     * @param id the id of the reactionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reactionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReactionDTO> getReaction(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Reaction : {}", id);
        Optional<ReactionDTO> reactionDTO = reactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reactionDTO);
    }

    /**
     * {@code DELETE  /reactions/:id} : delete the "id" reaction.
     *
     * @param id the id of the reactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReaction(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Reaction : {}", id);
        reactionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /reactions/_search?query=:query} : search for the reaction corresponding
     * to the query.
     *
     * @param query the query of the reaction search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<ReactionDTO>> searchReactions(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Reactions for query {}", query);
        try {
            Page<ReactionDTO> page = reactionService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }

    /**
     * {@code GET  /reactions/post/:postId} : get all the reactions for a post.
     *
     * @param postId the id of the post to retrieve reactions for.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reactions in body.
     */
    @GetMapping("/{postId}/reactions")
    public ResponseEntity<List<ReactionDTO>> getAllReactionsByPostId(
        @PathVariable Long postId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of Reactions for post {}", postId);
        Page<ReactionDTO> page = reactionService.findAllByPostId(postId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/posts/{postId}/liked")
    public ResponseEntity<Map<String, Boolean>> hasLiked(@PathVariable Long postId) {
        Long userId = SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .map(User::getId)
            .orElseThrow(() -> new RuntimeException("User chưa login"));

        boolean liked = reactionService.hasReacted(postId, userId);
        return ResponseEntity.ok(Collections.singletonMap("liked", liked));
    }
}
