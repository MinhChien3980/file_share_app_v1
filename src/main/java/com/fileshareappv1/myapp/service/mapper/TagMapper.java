package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.Tag;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.TagDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Tag} and its DTO {@link TagDTO}.
 */
@Mapper(componentModel = "spring")
public interface TagMapper extends EntityMapper<TagDTO, Tag> {
    @Mapping(target = "postIds", source = "posts")
    TagDTO toDto(Tag tag);

    @Mapping(target = "posts", ignore = true)
    Tag toEntity(TagDTO tagDTO);

    @Named("postId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PostDTO toDtoPostId(Post post);

    @Named("postIdSet")
    default Set<PostDTO> toDtoPostIdSet(Set<Post> post) {
        return post.stream().map(this::toDtoPostId).collect(Collectors.toSet());
    }

    default Set<Long> mapPostsToIds(Set<Post> posts) {
        if (posts == null) {
            return Set.of();
        }
        return posts.stream().map(Post::getId).collect(Collectors.toSet());
    }
}
