package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Page<Post> findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(String title, String content, String description, Pageable pageable);

    long countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(String title, String content, String description);
}
