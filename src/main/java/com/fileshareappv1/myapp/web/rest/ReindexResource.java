package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.domain.*;
import com.fileshareappv1.myapp.repository.*;
import com.fileshareappv1.myapp.repository.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReindexResource {

    private final Logger log = LoggerFactory.getLogger(ReindexResource.class);

    private final CommentRepository commentRepository;
    private final CommentSearchRepository commentSearchRepository;

    private final FavoriteRepository favoriteRepository;
    private final FavoriteSearchRepository favoriteSearchRepository;

    private final FileRepository fileRepository;
    private final FileSearchRepository fileSearchRepository;

    private final FollowRepository followRepository;
    private final FollowSearchRepository followSearchRepository;

    private final MentionRepository mentionRepository;
    private final MentionSearchRepository mentionSearchRepository;

    private final NotificationRepository notificationRepository;
    private final NotificationSearchRepository notificationSearchRepository;

    private final PostRepository postRepository;
    private final PostSearchRepository postSearchRepository;

    private final ReactionRepository reactionRepository;
    private final ReactionSearchRepository reactionSearchRepository;

    private final ShareRepository shareRepository;
    private final ShareSearchRepository shareSearchRepository;

    private final TagRepository tagRepository;
    private final TagSearchRepository tagSearchRepository;

    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;

    public ReindexResource(
        CommentRepository commentRepository,
        CommentSearchRepository commentSearchRepository,
        FavoriteRepository favoriteRepository,
        FavoriteSearchRepository favoriteSearchRepository,
        FileRepository fileRepository,
        FileSearchRepository fileSearchRepository,
        FollowRepository followRepository,
        FollowSearchRepository followSearchRepository,
        MentionRepository mentionRepository,
        MentionSearchRepository mentionSearchRepository,
        NotificationRepository notificationRepository,
        NotificationSearchRepository notificationSearchRepository,
        PostRepository postRepository,
        PostSearchRepository postSearchRepository,
        ReactionRepository reactionRepository,
        ReactionSearchRepository reactionSearchRepository,
        ShareRepository shareRepository,
        ShareSearchRepository shareSearchRepository,
        TagRepository tagRepository,
        TagSearchRepository tagSearchRepository,
        UserRepository userRepository,
        UserSearchRepository userSearchRepository
    ) {
        this.commentRepository = commentRepository;
        this.commentSearchRepository = commentSearchRepository;
        this.favoriteRepository = favoriteRepository;
        this.favoriteSearchRepository = favoriteSearchRepository;
        this.fileRepository = fileRepository;
        this.fileSearchRepository = fileSearchRepository;
        this.followRepository = followRepository;
        this.followSearchRepository = followSearchRepository;
        this.mentionRepository = mentionRepository;
        this.mentionSearchRepository = mentionSearchRepository;
        this.notificationRepository = notificationRepository;
        this.notificationSearchRepository = notificationSearchRepository;
        this.postRepository = postRepository;
        this.postSearchRepository = postSearchRepository;
        this.reactionRepository = reactionRepository;
        this.reactionSearchRepository = reactionSearchRepository;
        this.shareRepository = shareRepository;
        this.shareSearchRepository = shareSearchRepository;
        this.tagRepository = tagRepository;
        this.tagSearchRepository = tagSearchRepository;
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
    }

    /**
     * {@code POST  /_reindex} : Đẩy lại toàn bộ dữ liệu từ MySQL lên Elasticsearch cho tất cả các entity.
     *
     * @return {@link ResponseEntity} với status {@code 200 (OK)} khi hoàn thành.
     */
    @PostMapping("/_reindex")
    public ResponseEntity<Void> reindexAll() {
        log.debug("REST request to reindex all entities");

        commentRepository.findAll().forEach(commentSearchRepository::index);
        favoriteRepository.findAll().forEach(favoriteSearchRepository::index);
        fileRepository.findAll().forEach(fileSearchRepository::index);
        followRepository.findAll().forEach(followSearchRepository::index);
        mentionRepository.findAll().forEach(mentionSearchRepository::index);
        notificationRepository.findAll().forEach(notificationSearchRepository::index);
        postRepository.findAll().forEach(postSearchRepository::index);
        reactionRepository.findAll().forEach(reactionSearchRepository::index);
        shareRepository.findAll().forEach(shareSearchRepository::index);
        tagRepository.findAll().forEach(tagSearchRepository::index);
        userRepository.findAll().forEach(userSearchRepository::index);

        return ResponseEntity.ok().build();
    }
}
