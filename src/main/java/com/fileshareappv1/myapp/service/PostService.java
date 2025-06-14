package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.repository.PostRepository;
import com.fileshareappv1.myapp.repository.search.PostSearchRepository;
import com.fileshareappv1.myapp.service.dto.FileDTO;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.mapper.PostMapper;
import com.fileshareappv1.myapp.service.storage.StorageRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private final StorageRepository storageRepository;
    private final FileService fileService;

    public PostService(
        PostRepository postRepository,
        PostMapper postMapper,
        PostSearchRepository postSearchRepository,
        StorageRepository storageRepository,
        FileService fileService
    ) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.postSearchRepository = postSearchRepository;
        this.storageRepository = storageRepository;
        this.fileService = fileService;
    }

    /**
     * Save a post with current user and proper tag handling.
     *
     * @param postDTO the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public PostDTO saveWithCurrentUser(PostDTO postDTO) {
        LOG.debug("Request to save Post with current user : {}", postDTO);

        // Ensure the post doesn't have an ID (for new posts)
        postDTO.setId(null);

        Post post = postMapper.toEntity(postDTO);
        post = postRepository.save(post);
        postSearchRepository.index(post);

        // Reload with eager relationships to get proper tag data
        Post reloaded = postRepository.findOneWithEagerRelationships(post.getId()).orElse(post);
        return postMapper.toDto(reloaded);
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
        return postRepository.findAll(pageable).map(this::convertFilesToUrls);
    }

    /**
     * Get all the posts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PostDTO> findAllWithEagerRelationships(Pageable pageable) {
        return postRepository.findAllWithEagerRelationships(pageable).map(this::convertFilesToUrls);
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
        return postRepository.findOneWithEagerRelationships(id).map(this::convertFilesToUrls);
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
        return postSearchRepository.search(query, pageable).map(this::convertFilesToUrls);
    }

    public Page<PostDTO> findMyPosts(Pageable pageable) {
        LOG.debug("Request to get all Posts");
        return postRepository.findByCurrentUser(pageable).map(this::convertFilesToUrls);
    }

    /**
     * Convert a Post entity to PostDTO and convert file names to full URLs
     */
    private PostDTO convertFilesToUrls(Post post) {
        PostDTO dto = postMapper.toDto(post);
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            List<String> fileUrls = dto
                .getFiles()
                .stream()
                .map(fileName ->
                    ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/").path(fileName).toUriString()
                )
                .collect(Collectors.toList());
            dto.setFiles(fileUrls);
        }
        return dto;
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
        // 1. Save the Post entity
        Post post = postMapper.toEntity(postDTO);
        post = postRepository.save(post);
        postSearchRepository.index(post);

        List<FileDTO> fileDtos = List.of();
        if (files != null && !files.isEmpty()) {
            fileDtos = this.storeFilesForPost(post.getId(), files);
        }
        Post reloaded = postRepository.findOneWithEagerRelationships(post.getId()).orElse(post);
        PostDTO result = postMapper.toDto(reloaded);

        List<String> urls = fileDtos.stream().map(FileDTO::getFileUrl).collect(Collectors.toList());
        result.setFiles(urls);
        result.setNumFiles(urls.size());

        return result;
    }

    @Transactional
    public List<FileDTO> storeFilesForPost(Long postId, List<MultipartFile> files) {
        // 1. Lưu lên disk, thu list tên
        List<String> storedNames = files.stream().map(storageRepository::store).toList();

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

    public Page<PostDTO> findByCurrentUser(Pageable pageable) {
        LOG.debug("Request to get all Posts for current user");
        return postRepository.findByCurrentUser(pageable).map(postMapper::toDto);
    }

    public Page<PostDTO> findByTags(List<String> tagNames, Pageable pageable) {
        LOG.debug("Request to get Posts by tags: {}", tagNames);
        return postRepository.findByTags(tagNames, pageable).map(postMapper::toDto);
    }
}
