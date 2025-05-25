package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.domain.Tag;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.repository.PostRepository;
import com.fileshareappv1.myapp.repository.TagRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.security.SecurityUtils;
import com.fileshareappv1.myapp.service.PostService;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.TagDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import com.fileshareappv1.myapp.web.rest.errors.BadRequestAlertException;
import com.fileshareappv1.myapp.web.rest.errors.ElasticsearchExceptionMapper;
import com.fileshareappv1.myapp.web.rest.form.PostForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.fileshareappv1.myapp.domain.Post}.
 */
@RestController
@RequestMapping("/api/posts")
public class PostResource {

    private static final Logger LOG = LoggerFactory.getLogger(PostResource.class);

    private static final String ENTITY_NAME = "post";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PostService postService;

    private final PostRepository postRepository;

    private final UserRepository userRepository;

    private final TagRepository tagRepository;

    public PostResource(
        PostService postService,
        PostRepository postRepository,
        UserRepository userRepository,
        TagRepository tagRepository
    ) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * {@code POST  /posts} : Create a new post.
     *
     * @param postDTO the postDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new postDTO, or with status {@code 400 (Bad Request)} if the post has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO) throws URISyntaxException {
        LOG.debug("REST request to save Post : {}", postDTO);
        if (postDTO.getId() != null) {
            throw new BadRequestAlertException("A new post cannot already have an ID", ENTITY_NAME, "idexists");
        }
        postDTO = postService.save(postDTO);
        return ResponseEntity.created(new URI("/api/posts/" + postDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, postDTO.getId().toString()))
            .body(postDTO);
    }

    /**
     * {@code PUT  /posts/:id} : Updates an existing post.
     *
     * @param id the id of the postDTO to save.
     * @param postDTO the postDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated postDTO,
     * or with status {@code 400 (Bad Request)} if the postDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the postDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PostDTO postDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Post : {}, {}", id, postDTO);
        if (postDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, postDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        postDTO = postService.update(postDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, postDTO.getId().toString()))
            .body(postDTO);
    }

    /**
     * {@code PATCH  /posts/:id} : Partial updates given fields of an existing post, field will ignore if it is null
     *
     * @param id the id of the postDTO to save.
     * @param postDTO the postDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated postDTO,
     * or with status {@code 400 (Bad Request)} if the postDTO is not valid,
     * or with status {@code 404 (Not Found)} if the postDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the postDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PostDTO> partialUpdatePost(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PostDTO postDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Post partially : {}, {}", id, postDTO);
        if (postDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, postDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PostDTO> result = postService.partialUpdate(postDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, postDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /posts} : get all the posts.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of posts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PostDTO>> getAllPosts(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Posts");
        Page<PostDTO> page;
        if (eagerload) {
            page = postService.findAllWithEagerRelationships(pageable);
        } else {
            page = postService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /posts/:id} : get the "id" post.
     *
     * @param id the id of the postDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the postDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Post : {}", id);
        Optional<PostDTO> postDTO = postService.findOne(id);
        return ResponseUtil.wrapOrNotFound(postDTO);
    }

    /**
     * {@code DELETE  /posts/:id} : delete the "id" post.
     *
     * @param id the id of the postDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Post : {}", id);
        postService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /posts/_search?query=:query} : search for the post corresponding
     * to the query.
     *
     * @param query the query of the post search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<PostDTO>> searchPosts(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Posts for query {}", query);
        try {
            Page<PostDTO> page = postService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }

    /**
     * GET  /posts/me : get a page of Posts of the currently logged in user.
     */
    @GetMapping("/me")
    public ResponseEntity<List<PostDTO>> getMyPosts(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        try {
            Page<PostDTO> page = postService.findMyPosts(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (Exception e) {
            LOG.error("Error occurred while fetching posts for user {}: {}", SecurityUtils.getCurrentUserLogin(), e.getMessage());
            throw e;
        }
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPostWithFiles(@Valid @ModelAttribute PostForm form) throws URISyntaxException {
        // 1. Build PostDTO từ form (không set user, tags ở đây)
        PostDTO dto = new PostDTO();
        dto.setContent(form.getContent());
        dto.setPrivacy(form.getPrivacy());
        // 2. Lấy User hiện tại từ Security
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User chưa login"));
        User user = userRepository.findOneByLogin(login).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        dto.setUser(new UserDTO(user));

        // 3. Lấy Tag entities từ tagIds
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(form.getTagIds()));
        dto.setTags(
            tags
                .stream()
                .map(tag -> {
                    var t = new TagDTO();
                    t.setId(tag.getId());
                    t.setName(tag.getName());
                    return t;
                })
                .collect(Collectors.toSet())
        );

        // 4. Gọi service lưu post + files + tags + user
        PostDTO result = postService.saveWithFiles(dto, form.getFiles());

        return ResponseEntity.created(new URI("/api/posts/" + result.getId())).body(result);
    }
}
