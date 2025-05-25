package com.fileshareappv1.myapp.service;

import co.elastic.clients.util.DateTime;
import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.repository.PostRepository;
import com.fileshareappv1.myapp.repository.search.PostSearchRepository;
import com.fileshareappv1.myapp.service.dto.FileDTO;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.mapper.PostMapper;
import com.fileshareappv1.myapp.service.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Service Implementation for managing {@link com.fileshareappv1.myapp.domain.Post}.
 */
@Service
@Transactional
public class PostService {

    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;

    private final PostMapper postMapper;

    private final PostSearchRepository postSearchRepository;
    private final StorageService storageService;
    private final FileService fileService;

    public PostService(
        PostRepository postRepository,
        PostMapper postMapper,
        PostSearchRepository postSearchRepository,
        StorageService storageService,
        FileService fileService
    ) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.postSearchRepository = postSearchRepository;
        this.storageService = storageService;
        this.fileService = fileService;
    }

    /**
     * Save a post.
     *
     * @param postDTO the entity to save.
     * @return the persisted entity.
     */
    public PostDTO save(PostDTO postDTO) {
        LOG.debug("Request to save Post : {}", postDTO);
        Post post = postMapper.toEntity(postDTO);
        post = postRepository.save(post);
        postSearchRepository.index(post);
        return postMapper.toDto(post);
    }

    /**
     * Update a post.
     *
     * @param postDTO the entity to save.
     * @return the persisted entity.
     */
    public PostDTO update(PostDTO postDTO) {
        LOG.debug("Request to update Post : {}", postDTO);
        Post post = postMapper.toEntity(postDTO);
        post = postRepository.save(post);
        postSearchRepository.index(post);
        return postMapper.toDto(post);
    }

    /**
     * Partially update a post.
     *
     * @param postDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PostDTO> partialUpdate(PostDTO postDTO) {
        LOG.debug("Request to partially update Post : {}", postDTO);

        return postRepository
            .findById(postDTO.getId())
            .map(existingPost -> {
                postMapper.partialUpdate(existingPost, postDTO);

                return existingPost;
            })
            .map(postRepository::save)
            .map(savedPost -> {
                postSearchRepository.index(savedPost);
                return savedPost;
            })
            .map(postMapper::toDto);
    }

    /**
     * Get all the posts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PostDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Posts");
        return postRepository.findAll(pageable).map(postMapper::toDto);
    }

    /**
     * Get all the posts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PostDTO> findAllWithEagerRelationships(Pageable pageable) {
        return postRepository.findAllWithEagerRelationships(pageable).map(postMapper::toDto);
    }

    /**
     * Get one post by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PostDTO> findOne(Long id) {
        LOG.debug("Request to get Post : {}", id);
        return postRepository.findOneWithEagerRelationships(id).map(postMapper::toDto);
    }

    /**
     * Delete the post by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Post : {}", id);
        postRepository.deleteById(id);
        postSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the post corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PostDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Posts for query {}", query);
        return postSearchRepository.search(query, pageable).map(postMapper::toDto);
    }

    public Page<PostDTO> findMyPosts(Pageable pageable) {
        LOG.debug("Request to get all Posts");
        return postRepository.findByCurrentUser(pageable).map(postMapper::toDto);
    }

    @Transactional
    public Post addFiles(Long postId, List<String> storedNames) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post không tồn tại: " + postId));
        post.getFiles().addAll(storedNames);
        post.setNumFiles(post.getFiles().size());
        return postRepository.save(post);
    }

    /**
     * Lưu Post và các file đính kèm.
     *
     * @param postDTO Thông tin Post cần lưu.
     * @param files Danh sách file đính kèm.
     * @return PostDTO đã lưu, bao gồm thông tin file và số lượng file.
     */
    @Transactional
    public PostDTO saveWithFiles(PostDTO postDTO, List<MultipartFile> files) {
        // 1. Tạo post trước
        Post post = postMapper.toEntity(postDTO);
        post = postRepository.save(post);
        postSearchRepository.index(post);

        // 2. Nếu có files đính kèm, lưu tiếp
        if (files != null && !files.isEmpty()) {
            this.storeFilesForPost(post.getId(), files);
        }
        Post reloaded = postRepository.findOneWithEagerRelationships(post.getId()).orElse(post);
        // 3. Trả về DTO mới, đã bao gồm files + numFiles
        return postMapper.toDto(reloaded);
    }

    @Transactional
    public List<FileDTO> storeFilesForPost(Long postId, List<MultipartFile> files) {
        // 1. Lưu lên disk, thu list tên
        List<String> storedNames = files.stream().map(storageService::store).toList();

        // 2. Ghi tên vào Post.files và update numFiles
        this.addFiles(postId, storedNames);

        // 3. Tạo và lưu FileDTO
        List<FileDTO> dtos = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            String name = storedNames.get(i);
            FileDTO dto = new FileDTO();
            dto.setFileName(name);
            dto.setMimeType(Optional.ofNullable(f.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
            dto.setFileSize(f.getSize());
            dto.setUploadedAt(Instant.now());
            dto.setFileUrl(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/").path(name).toUriString());
            dtos.add(fileService.save(dto));
        }
        return dtos;
    }
}
