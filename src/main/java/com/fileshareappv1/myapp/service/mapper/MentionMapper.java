package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Mention;
import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.MentionDTO;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Mention} and its DTO {@link MentionDTO}.
 */
@Mapper(componentModel = "spring")
public interface MentionMapper extends EntityMapper<MentionDTO, Mention> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    MentionDTO toDto(Mention s);

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
