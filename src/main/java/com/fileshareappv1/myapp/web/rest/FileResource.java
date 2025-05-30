package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.domain.File;
import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.repository.FileRepository;
import com.fileshareappv1.myapp.repository.PostRepository;
import com.fileshareappv1.myapp.service.FileService;
import com.fileshareappv1.myapp.service.dto.FileDTO;
import com.fileshareappv1.myapp.service.storage.StorageRepository;
import com.fileshareappv1.myapp.web.rest.errors.BadRequestAlertException;
import com.fileshareappv1.myapp.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
 * REST controller for managing {@link File}.
 */
@RestController
@RequestMapping("/api/files")
public class FileResource {

    private static final Logger LOG = LoggerFactory.getLogger(FileResource.class);

    private static final String ENTITY_NAME = "file";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FileService fileService;

    private final FileRepository fileRepository;

    private final StorageRepository storageRepository;
    private final PostRepository postRepository;

    public FileResource(
        FileService fileService,
        FileRepository fileRepository,
        StorageRepository storageRepository,
        PostRepository postRepository
    ) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
        this.storageRepository = storageRepository;
        this.postRepository = postRepository;
    }

    /**
     * {@code POST  /files} : Create a new file.
     *
     * @param fileDTO the fileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fileDTO, or with status {@code 400 (Bad Request)} if the file has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FileDTO> createFile(@Valid @RequestBody FileDTO fileDTO) throws URISyntaxException {
        LOG.debug("REST request to save File : {}", fileDTO);
        if (fileDTO.getId() != null) {
            throw new BadRequestAlertException("A new file cannot already have an ID", ENTITY_NAME, "idexists");
        }
        fileDTO = fileService.save(fileDTO);
        return ResponseEntity.created(new URI("/api/files/" + fileDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, fileDTO.getId().toString()))
            .body(fileDTO);
    }

    /**
     * {@code PUT  /files/:id} : Updates an existing file.
     *
     * @param id the id of the fileDTO to save.
     * @param fileDTO the fileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fileDTO,
     * or with status {@code 400 (Bad Request)} if the fileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FileDTO> updateFile(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FileDTO fileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update File : {}, {}", id, fileDTO);
        if (fileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        fileDTO = fileService.update(fileDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fileDTO.getId().toString()))
            .body(fileDTO);
    }

    /**
     * {@code PATCH  /files/:id} : Partial updates given fields of an existing file, field will ignore if it is null
     *
     * @param id the id of the fileDTO to save.
     * @param fileDTO the fileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fileDTO,
     * or with status {@code 400 (Bad Request)} if the fileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the fileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the fileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FileDTO> partialUpdateFile(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FileDTO fileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update File partially : {}, {}", id, fileDTO);
        if (fileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FileDTO> result = fileService.partialUpdate(fileDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fileDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /files} : get all the files.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of files in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FileDTO>> getAllFiles(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Files");
        Page<FileDTO> page = fileService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /files/:id} : get the "id" file.
     *
     * @param id the id of the fileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileDTO> getFile(@PathVariable("id") Long id) {
        LOG.debug("REST request to get File : {}", id);
        Optional<FileDTO> fileDTO = fileService.findOne(id);
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    /**
     * {@code DELETE  /files/:id} : delete the "id" file.
     *
     * @param id the id of the fileDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete File : {}", id);
        fileService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /files/_search?query=:query} : search for the file corresponding
     * to the query.
     *
     * @param query the query of the file search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<FileDTO>> searchFiles(@RequestParam("query") String query, @ParameterObject Pageable pageable) {
        LOG.debug("REST request to search for a page of Files for query {}", query);
        try {
            Page<FileDTO> page = fileService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<FileDTO> uploadFile(@RequestParam("file") MultipartFile file) throws URISyntaxException {
        String storedFilename = storageRepository.store(file);

        FileDTO dto = new FileDTO();
        dto.setFileName(file.getOriginalFilename());
        dto.setMimeType(Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        dto.setFileSize(file.getSize());
        dto.setUploadedAt(Instant.now());
        dto.setFileUrl(
            ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/").path(storedFilename).toUriString()
        );
        // (if you have a postId to associate: dto.setPost(somePostDto); )

        FileDTO result = fileService.save(dto);

        return ResponseEntity.created(new URI("/api/files/" + result.getId())).body(result);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
        Resource resource = storageRepository.loadAsResource(filename);

        String contentType;
        try {
            String absolutePath = resource.getFile().getAbsolutePath();
            contentType = request.getServletContext().getMimeType(absolutePath);
            if (contentType == null) {
                contentType = Files.probeContentType(Path.of(absolutePath));
            }
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    /**
     * POST /api/files/upload-multiple
     * Accepts N files under the form‐field “files” and returns
     * a JSON array of saved FileDTOs (id, filename, mimeType, url, …).
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileDTO>> uploadMultipleFiles(
        @RequestParam("files") List<MultipartFile> files,
        @RequestParam("postId") Long postId
    ) throws URISyntaxException {
        // 1. Store tất cả file, thu về list tên (UUID.ext)
        List<String> storedNames = files.stream().map(storageRepository::store).toList();

        // 2. Cập nhật Post chỉ một lần
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post không tồn tại: " + postId));

        // files đã luôn non-null vì khởi tạo mặc định ở entity
        post.setFiles(storedNames);

        // Đếm số file, gán vào numFiles
        post.setNumFiles(post.getFiles().size());

        postRepository.save(post);

        // 3. Tạo và lưu FileDTO cho từng storedName
        List<FileDTO> savedDTOs = new ArrayList<>();
        for (int i = 0; i < storedNames.size(); i++) {
            String name = storedNames.get(i);
            MultipartFile original = files.get(i);

            FileDTO dto = new FileDTO();
            dto.setFileName(name);
            dto.setMimeType(Optional.ofNullable(original.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
            dto.setFileSize(original.getSize());
            dto.setUploadedAt(Instant.now());
            dto.setFileUrl(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/").path(name).toUriString());

            savedDTOs.add(fileService.save(dto));
        }

        return ResponseEntity.created(new URI("/api/files/upload-multiple")).body(savedDTOs);
    }

    /**
     * GET  /{postId}/files : get a page of FileDTOs for a given Post.
     */
    @GetMapping("/{postId}/files")
    public ResponseEntity<List<FileDTO>> getFilesByPostId(@PathVariable Long postId, @ParameterObject Pageable pageable) {
        LOG.debug("REST request to get Files by Post : {}", postId);
        Page<FileDTO> page = fileService.findAllByPostId(postId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
