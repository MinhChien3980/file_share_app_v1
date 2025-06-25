package com.fileshareappv1.myapp.repository;

import com.fileshareappv1.myapp.domain.Reaction;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Reaction entity.
 */
@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    @Query("select reaction from Reaction reaction where reaction.user.login = ?#{authentication.name}")
    List<Reaction> findByUserIsCurrentUser();

    default Optional<Reaction> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Reaction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Reaction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select reaction from Reaction reaction left join fetch reaction.user",
        countQuery = "select count(reaction) from Reaction reaction"
    )
    Page<Reaction> findAllWithToOneRelationships(Pageable pageable);

    @Query("select reaction from Reaction reaction left join fetch reaction.user")
    List<Reaction> findAllWithToOneRelationships();

    @Query("select reaction from Reaction reaction left join fetch reaction.user where reaction.id =:id")
    Optional<Reaction> findOneWithToOneRelationships(@Param("id") Long id);

    Page<Reaction> findAllByPostId(Long postId, Pageable pageable);

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
