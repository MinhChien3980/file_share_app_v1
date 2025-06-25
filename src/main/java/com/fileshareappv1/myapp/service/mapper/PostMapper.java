package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.Tag;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.TagDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Post} and its DTO {@link PostDTO}.
 */
@Mapper(componentModel = "spring")
public interface PostMapper extends EntityMapper<PostDTO, Post> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagNameSet")
    PostDTO toDto(Post s);

    @Mapping(target = "removeTags", ignore = true)
    Post toEntity(PostDTO postDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "imageUrl", source = "imageUrl")
    UserDTO toDtoUserLogin(User user);

    @Named("tagName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TagDTO toDtoTagName(Tag tag);

    @Named("tagNameSet")
    default Set<TagDTO> toDtoTagNameSet(Set<Tag> tag) {
        return tag.stream().map(this::toDtoTagName).collect(Collectors.toSet());
    }

    @Named("mapPostsToIds")
    default Set<Long> mapPostsToIds(Set<Post> posts) {
        if (posts == null) {
            return Set.of();
        }
        return posts.stream().map(Post::getId).collect(Collectors.toSet());
    }
}
