package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.Reaction;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.ReactionDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Reaction} and its DTO {@link ReactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReactionMapper extends EntityMapper<ReactionDTO, Reaction> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    ReactionDTO toDto(Reaction s);

    @Named("postId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PostDTO toDtoPostId(Post post);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
