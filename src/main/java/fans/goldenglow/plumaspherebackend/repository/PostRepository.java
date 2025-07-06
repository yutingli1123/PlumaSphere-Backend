package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing posts in the application.
 * Provides methods to find posts by tags, title, content, and description,
 * as well as methods to count posts based on these criteria.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTagsName(String tagsName, Pageable pageable);

    long countByTagsName(String tagsName);

    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countByKeyword(@Param("keyword") String keyword);
}
