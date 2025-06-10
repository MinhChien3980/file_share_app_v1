package com.fileshareappv1.myapp.repository;

import com.fileshareappv1.myapp.domain.Mention;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Mention entity.
 */
@Repository
public interface MentionRepository extends JpaRepository<Mention, Long> {
    @Query("select mention from Mention mention where mention.user.login = ?#{authentication.name}")
    List<Mention> findByUserIsCurrentUser();

    default Optional<Mention> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Mention> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Mention> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select mention from Mention mention left join fetch mention.user",
        countQuery = "select count(mention) from Mention mention"
    )
    Page<Mention> findAllWithToOneRelationships(Pageable pageable);

    @Query("select mention from Mention mention left join fetch mention.user")
    List<Mention> findAllWithToOneRelationships();

    @Query("select mention from Mention mention left join fetch mention.user where mention.id =:id")
    Optional<Mention> findOneWithToOneRelationships(@Param("id") Long id);
}
