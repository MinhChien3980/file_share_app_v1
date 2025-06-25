package com.fileshareappv1.myapp.repository;

import com.fileshareappv1.myapp.domain.File;
import java.nio.channels.FileChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the File entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Page<File> findAllByPostId(Long postId, Pageable pageable);
}
