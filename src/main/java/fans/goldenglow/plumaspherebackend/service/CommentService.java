package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing comments in the application.
 * Provides methods to find comments by post ID, parent comment ID, and author ID,
 * as well as methods to count comments by post and author.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    /**
     * Finds a comment by its ID.
     *
     * @param id the ID of the comment
     * @return an Optional containing the comment if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    /**
     * Finds comments associated with a specific post ID.
     *
     * @param postId   the ID of the post
     * @param pageable pagination information
     * @return a page of comments for the specified post
     */
    @Transactional(readOnly = true)
    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    /**
     * Finds comments made by a specific user.
     *
     * @param userId the ID of the user
     * @param pageable pagination information
     * @return a page of comments made by the specified user
     */
    @Transactional(readOnly = true)
    public Page<Comment> findByUserId(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorId(userId, pageable);
    }

    /**
     * Counts the number of comments made by a specific user.
     *
     * @param userId the ID of the user
     * @return the number of comments made by the specified user
     */
    @Transactional(readOnly = true)
    public Long countByUserId(Long userId) {
        return commentRepository.countByAuthorId(userId);
    }

    /**
     * Counts the number of comments associated with a specific post ID.
     *
     * @param postId the ID of the post
     * @return the number of comments for the specified post
     */
    @Transactional(readOnly = true)
    public long countByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * Finds the post ID associated with a specific comment ID.
     *
     * @param commentId the ID of the comment
     * @return the ID of the post associated with the comment, or null if not found
     */
    @Transactional(readOnly = true)
    public Long findPostId(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment
                .map(Comment::getPost)
                .map(Post::getId)
                .orElse(null);
    }

    /**
     * Saves a comment to the repository.
     *
     * @param comment the comment to save
     */
    @Transactional
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    /**
     * Finds comments that are replies to a specific parent comment ID.
     *
     * @param parentCommentId the ID of the parent comment
     * @param pageable pagination information
     * @return a page of comments that are replies to the specified parent comment
     */
    @Transactional(readOnly = true)
    public Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable) {
        return commentRepository.findByParentCommentId(parentCommentId, pageable);
    }

    /**
     * Deletes a comment from the repository.
     *
     * @param comment the comment to delete
     */
    @Transactional
    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }
}
