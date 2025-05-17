package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Favorite;
import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.FavoriteDTO;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Favorite} and its DTO {@link FavoriteDTO}.
 */
@Mapper(componentModel = "spring")
public interface FavoriteMapper extends EntityMapper<FavoriteDTO, Favorite> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    FavoriteDTO toDto(Favorite s);

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
