package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Comment;
import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.CommentDTO;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Comment} and its DTO {@link CommentDTO}.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper extends EntityMapper<CommentDTO, Comment> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "parentComment", source = "parentComment", qualifiedByName = "commentId")
    CommentDTO toDto(Comment s);

    @Named("postId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PostDTO toDtoPostId(Post post);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("commentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CommentDTO toDtoCommentId(Comment comment);
}
