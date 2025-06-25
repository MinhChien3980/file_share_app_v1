package com.fileshareappv1.myapp.service.mapper;

import com.fileshareappv1.myapp.domain.File;
import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.service.dto.FileDTO;
import com.fileshareappv1.myapp.service.dto.PostDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link File} and its DTO {@link FileDTO}.
 */
@Mapper(componentModel = "spring")
public interface FileMapper extends EntityMapper<FileDTO, File> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postId")
    FileDTO toDto(File s);

    @Named("postId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PostDTO toDtoPostId(Post post);
}
