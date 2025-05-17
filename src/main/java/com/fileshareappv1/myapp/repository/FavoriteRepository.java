package com.fileshareappv1.myapp.repository;

import com.fileshareappv1.myapp.domain.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Favorite entity.
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @Query("select favorite from Favorite favorite where favorite.user.login = ?#{authentication.name}")
    List<Favorite> findByUserIsCurrentUser();

    default Optional<Favorite> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Favorite> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Favorite> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select favorite from Favorite favorite left join fetch favorite.user",
        countQuery = "select count(favorite) from Favorite favorite"
    )
    Page<Favorite> findAllWithToOneRelationships(Pageable pageable);

    @Query("select favorite from Favorite favorite left join fetch favorite.user")
    List<Favorite> findAllWithToOneRelationships();

    @Query("select favorite from Favorite favorite left join fetch favorite.user where favorite.id =:id")
    Optional<Favorite> findOneWithToOneRelationships(@Param("id") Long id);
}
