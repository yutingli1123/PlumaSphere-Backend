package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findBytagsName(String tagName, Pageable pageable);

    long countByTagsName(String tagsName);

    Page<Post> findByTitleContainsIgnoreCaseOrContentContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String content, String description, Pageable pageable);
}
