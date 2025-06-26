package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing comments in the application.
 * Provides methods to find comments by post ID, parent comment ID, and author ID,
 * as well as methods to count comments by post and author.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    Long countByPostId(Long postId);

    Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    Long countByAuthorId(Long authorId);
}
