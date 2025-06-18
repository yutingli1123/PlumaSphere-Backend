package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("CommentRepository Tests")
class CommentRepositoryTest {

    private static final String TEST_USERNAME = "username";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_POST_TITLE = "title";
    private static final String TEST_POST_CONTENT = "content";
    private static final String TEST_POST_DESCRIPTION = "description";
    private static final String TEST_COMMENT_CONTENT = "testContent";
    private static final String PARENT_COMMENT_CONTENT = "parentContent";
    private static final String CHILD_COMMENT_CONTENT = "childContent";
    
    private final CommentRepository commentRepository;
    private final TestEntityManager entityManager;

    private User savedUser;
    private Post savedPost;

    @Autowired
    public CommentRepositoryTest(CommentRepository commentRepository, TestEntityManager entityManager) {
        this.commentRepository = commentRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD);
        savedUser = entityManager.persistAndFlush(user);

        Post post = new Post();
        post.setTitle(TEST_POST_TITLE);
        post.setContent(TEST_POST_CONTENT);
        post.setDescription(TEST_POST_DESCRIPTION);
        post.setAuthor(savedUser);
        savedPost = entityManager.persistAndFlush(post);
    }

    @Nested
    @DisplayName("Post Operations")
    class PostOperationsTests {
        @Test
        @DisplayName("Should return comments when comments exist for post")
        void findByPostId_ShouldReturnComments_WhenCommentsExist() {
            // Given
            Long postId = savedPost.getId();
            Comment comment = createComment(TEST_COMMENT_CONTENT, savedPost, savedUser);
            entityManager.persistAndFlush(comment);

            // When
            Page<Comment> comments = commentRepository.findByPostId(postId, PageRequest.of(0, 10));

            // Then
            assertThat(comments)
                    .hasSize(1)
                    .first()
                    .satisfies(c -> {
                        assertThat(c.getContent()).isEqualTo(TEST_COMMENT_CONTENT);
                        assertThat(c.getPost().getId()).isEqualTo(postId);
                        assertThat(c.getAuthor().getUsername()).isEqualTo(TEST_USERNAME);
                    });
        }

        @Test
        @DisplayName("Should return empty when no comments exist for post")
        void findByPostId_ShouldReturnEmpty_WhenNoCommentsExist() {
            // Given
            Long postId = savedPost.getId();

            // When
            Page<Comment> comments = commentRepository.findByPostId(postId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when post does not exist")
        void findByPostId_ShouldReturnEmpty_WhenPostDoesNotExist() {
            // Given
            Long nonExistentPostId = 99999L;

            // When
            Page<Comment> comments = commentRepository.findByPostId(nonExistentPostId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple comments for post")
        void findByPostId_ShouldHandleMultipleComments() {
            // Given
            Long postId = savedPost.getId();
            Comment comment1 = createComment("comment1", savedPost, savedUser);
            Comment comment2 = createComment("comment2", savedPost, savedUser);
            entityManager.persistAndFlush(comment1);
            entityManager.persistAndFlush(comment2);

            // When
            Page<Comment> comments = commentRepository.findByPostId(postId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).hasSize(2);
        }

        @Test
        @DisplayName("Should return correct count when comments exist for post")
        void countByPostId_ShouldReturnCorrectCount_WhenCommentsExist() {
            // Given
            Long postId = savedPost.getId();
            Comment comment = createComment(TEST_COMMENT_CONTENT, savedPost, savedUser);
            entityManager.persistAndFlush(comment);

            // When
            Long count = commentRepository.countByPostId(postId);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return zero when no comments exist for post")
        void countByPostId_ShouldReturnZero_WhenNoCommentsExist() {
            // Given
            Long postId = savedPost.getId();

            // When
            Long count = commentRepository.countByPostId(postId);

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero when post does not exist")
        void countByPostId_ShouldReturnZero_WhenPostDoesNotExist() {
            // Given
            Long nonExistentPostId = 99999L;

            // When
            Long count = commentRepository.countByPostId(nonExistentPostId);

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle pagination for post comments")
        void findMethods_ShouldHandlePagination() {
            // Given - Create multiple comments
            Long postId = savedPost.getId();
            for (int i = 1; i <= 5; i++) {
                Comment comment = createComment("comment" + i, savedPost, savedUser);
                entityManager.persistAndFlush(comment);
            }

            // When - Request page with size 3
            Page<Comment> page = commentRepository.findByPostId(postId, PageRequest.of(0, 3));

            // Then
            assertThat(page.getSize()).isEqualTo(3);
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("Count methods should be consistent with find methods for post")
        void countMethods_ShouldBeConsistent_WithFindMethods() {
            // Given
            Long postId = savedPost.getId();
            Long authorId = savedUser.getId();

            Comment comment = createComment(TEST_COMMENT_CONTENT, savedPost, savedUser);
            entityManager.persistAndFlush(comment);

            // When
            Page<Comment> foundByPost = commentRepository.findByPostId(postId, PageRequest.of(0, 10));
            Long countByPost = commentRepository.countByPostId(postId);

            Page<Comment> foundByAuthor = commentRepository.findByAuthorId(authorId, PageRequest.of(0, 10));
            Long countByAuthor = commentRepository.countByAuthorId(authorId);

            // Then
            assertThat(foundByPost.getTotalElements()).isEqualTo(countByPost);
            assertThat(foundByAuthor.getTotalElements()).isEqualTo(countByAuthor);
        }
    }

    @Nested
    @DisplayName("Parent Comment Operations")
    class ParentCommentOperationsTests {
        @Test
        @DisplayName("Should return child comments when they exist")
        void findByParentCommentId_ShouldReturnChildComments_WhenChildCommentsExist() {
            // Given
            Comment parentComment = createComment(PARENT_COMMENT_CONTENT, null, savedUser);
            Comment savedParentComment = entityManager.persistAndFlush(parentComment);
            Long parentCommentId = savedParentComment.getId();

            Comment childComment = createComment(CHILD_COMMENT_CONTENT, null, savedUser);
            childComment.setParentComment(savedParentComment);
            entityManager.persistAndFlush(childComment);

            // When
            Page<Comment> comments = commentRepository.findByParentCommentId(parentCommentId, PageRequest.of(0, 10));

            // Then
            assertThat(comments)
                    .hasSize(1)
                    .first()
                    .satisfies(c -> {
                        assertThat(c.getContent()).isEqualTo(CHILD_COMMENT_CONTENT);
                        assertThat(c.getParentComment().getId()).isEqualTo(parentCommentId);
                    });
        }

        @Test
        @DisplayName("Should return empty when no child comments exist")
        void findByParentCommentId_ShouldReturnEmpty_WhenNoChildCommentsExist() {
            // Given
            Comment parentComment = createComment(PARENT_COMMENT_CONTENT, null, savedUser);
            Comment savedParentComment = entityManager.persistAndFlush(parentComment);
            Long parentCommentId = savedParentComment.getId();

            // When
            Page<Comment> comments = commentRepository.findByParentCommentId(parentCommentId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when parent comment does not exist")
        void findByParentCommentId_ShouldReturnEmpty_WhenParentCommentDoesNotExist() {
            // Given
            Long nonExistentParentId = 99999L;

            // When
            Page<Comment> comments = commentRepository.findByParentCommentId(nonExistentParentId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).isEmpty();
        }
    }

    @Nested
    @DisplayName("Author Operations")
    class AuthorOperationsTests {
        @Test
        @DisplayName("Should return comments when author has comments")
        void findByAuthorId_ShouldReturnComments_WhenAuthorHasComments() {
            // Given
            Long authorId = savedUser.getId();
            Comment comment = createComment(TEST_COMMENT_CONTENT, null, savedUser);
            entityManager.persistAndFlush(comment);

            // When
            Page<Comment> comments = commentRepository.findByAuthorId(authorId, PageRequest.of(0, 10));

            // Then
            assertThat(comments)
                    .hasSize(1)
                    .first()
                    .satisfies(c -> {
                        assertThat(c.getContent()).isEqualTo(TEST_COMMENT_CONTENT);
                        assertThat(c.getAuthor().getId()).isEqualTo(authorId);
                    });
        }

        @Test
        @DisplayName("Should return empty when author has no comments")
        void findByAuthorId_ShouldReturnEmpty_WhenAuthorHasNoComments() {
            // Given
            User anotherUser = new User("anotherUser", "password");
            User savedAnotherUser = entityManager.persistAndFlush(anotherUser);
            Long authorId = savedAnotherUser.getId();

            // When
            Page<Comment> comments = commentRepository.findByAuthorId(authorId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when author does not exist")
        void findByAuthorId_ShouldReturnEmpty_WhenAuthorDoesNotExist() {
            // Given
            Long nonExistentAuthorId = 99999L;

            // When
            Page<Comment> comments = commentRepository.findByAuthorId(nonExistentAuthorId, PageRequest.of(0, 10));

            // Then
            assertThat(comments).isEmpty();
        }

        @Test
        @DisplayName("Should return correct count when author has comments")
        void countByAuthorId_ShouldReturnCorrectCount_WhenAuthorHasComments() {
            // Given
            Long authorId = savedUser.getId();
            Comment comment1 = createComment("comment1", null, savedUser);
            Comment comment2 = createComment("comment2", null, savedUser);
            entityManager.persistAndFlush(comment1);
            entityManager.persistAndFlush(comment2);

            // When
            Long count = commentRepository.countByAuthorId(authorId);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero when author has no comments")
        void countByAuthorId_ShouldReturnZero_WhenAuthorHasNoComments() {
            // Given
            User anotherUser = new User("anotherUser", "password");
            User savedAnotherUser = entityManager.persistAndFlush(anotherUser);
            Long authorId = savedAnotherUser.getId();

            // When
            Long count = commentRepository.countByAuthorId(authorId);

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero when author does not exist")
        void countByAuthorId_ShouldReturnZero_WhenAuthorDoesNotExist() {
            // Given
            Long nonExistentAuthorId = 99999L;

            // When
            Long count = commentRepository.countByAuthorId(nonExistentAuthorId);

            // Then
            assertThat(count).isEqualTo(0);
        }
    }

    private Comment createComment(String content, Post post, User author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        return comment;
    }
}
