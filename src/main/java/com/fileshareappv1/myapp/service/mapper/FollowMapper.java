package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.Follow;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.service.dto.FollowDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Follow} and its DTO {@link FollowDTO}.
 */
@Mapper(componentModel = "spring")
public interface FollowMapper extends EntityMapper<FollowDTO, Follow> {
    @Mapping(target = "follower", source = "follower", qualifiedByName = "userLogin")
    @Mapping(target = "following", source = "following", qualifiedByName = "userLogin")
    FollowDTO toDto(Follow s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
