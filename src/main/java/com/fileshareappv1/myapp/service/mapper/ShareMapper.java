package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.Share;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.ShareDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Share} and its DTO {@link ShareDTO}.
 */
@Mapper(componentModel = "spring")
public interface ShareMapper extends EntityMapper<ShareDTO, Share> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    ShareDTO toDto(Share s);

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
