package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryTest {

    private static final String TEST_USERNAME = "username";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_POST_TITLE = "title";
    private static final String TEST_POST_CONTENT = "content";
    private static final String TEST_POST_DESCRIPTION = "description";
    private static final String TEST_COMMENT_CONTENT = "testContent";
    private static final String PARENT_COMMENT_CONTENT = "parentContent";
    private static final String CHILD_COMMENT_CONTENT = "childContent";
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private User savedUser;
    private Post savedPost;

    @Autowired
    public CommentRepositoryTest(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @BeforeEach
    void setUp() {
        User user = new User(TEST_USERNAME, TEST_PASSWORD);
        savedUser = userRepository.save(user);

        Post post = new Post();
        post.setTitle(TEST_POST_TITLE);
        post.setContent(TEST_POST_CONTENT);
        post.setDescription(TEST_POST_DESCRIPTION);
        post.setAuthor(savedUser);
        savedPost = postRepository.save(post);
    }

    @Test
    void findByPostId_ShouldReturnComments_WhenCommentsExist() {
        // Given
        Long postId = savedPost.getId();
        Comment comment = createComment(TEST_COMMENT_CONTENT, savedPost, savedUser);
        commentRepository.save(comment);

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
    void findByPostId_ShouldReturnEmpty_WhenNoCommentsExist() {
        // Given
        Long postId = savedPost.getId();

        // When
        Page<Comment> comments = commentRepository.findByPostId(postId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void findByPostId_ShouldReturnEmpty_WhenPostDoesNotExist() {
        // Given
        Long nonExistentPostId = 99999L;

        // When
        Page<Comment> comments = commentRepository.findByPostId(nonExistentPostId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void findByPostId_ShouldHandleMultipleComments() {
        // Given
        Long postId = savedPost.getId();
        Comment comment1 = createComment("comment1", savedPost, savedUser);
        Comment comment2 = createComment("comment2", savedPost, savedUser);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // When
        Page<Comment> comments = commentRepository.findByPostId(postId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).hasSize(2);
    }

    @Test
    void countByPostId_ShouldReturnCorrectCount_WhenCommentsExist() {
        // Given
        Long postId = savedPost.getId();
        Comment comment = createComment(TEST_COMMENT_CONTENT, savedPost, savedUser);
        commentRepository.save(comment);

        // When
        Long count = commentRepository.countByPostId(postId);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByPostId_ShouldReturnZero_WhenNoCommentsExist() {
        // Given
        Long postId = savedPost.getId();

        // When
        Long count = commentRepository.countByPostId(postId);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void countByPostId_ShouldReturnZero_WhenPostDoesNotExist() {
        // Given
        Long nonExistentPostId = 99999L;

        // When
        Long count = commentRepository.countByPostId(nonExistentPostId);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByParentCommentId_ShouldReturnChildComments_WhenChildCommentsExist() {
        // Given
        Comment parentComment = createComment(PARENT_COMMENT_CONTENT, null, savedUser);
        Comment savedParentComment = commentRepository.save(parentComment);
        Long parentCommentId = savedParentComment.getId();

        Comment childComment = createComment(CHILD_COMMENT_CONTENT, null, savedUser);
        childComment.setParentComment(savedParentComment);
        commentRepository.save(childComment);

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
    void findByParentCommentId_ShouldReturnEmpty_WhenNoChildCommentsExist() {
        // Given
        Comment parentComment = createComment(PARENT_COMMENT_CONTENT, null, savedUser);
        Comment savedParentComment = commentRepository.save(parentComment);
        Long parentCommentId = savedParentComment.getId();

        // When
        Page<Comment> comments = commentRepository.findByParentCommentId(parentCommentId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void findByParentCommentId_ShouldReturnEmpty_WhenParentCommentDoesNotExist() {
        // Given
        Long nonExistentParentId = 99999L;

        // When
        Page<Comment> comments = commentRepository.findByParentCommentId(nonExistentParentId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void findByAuthorId_ShouldReturnComments_WhenAuthorHasComments() {
        // Given
        Long authorId = savedUser.getId();
        Comment comment = createComment(TEST_COMMENT_CONTENT, null, savedUser);
        commentRepository.save(comment);

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
    void findByAuthorId_ShouldReturnEmpty_WhenAuthorHasNoComments() {
        // Given
        User anotherUser = new User("anotherUser", "password");
        User savedAnotherUser = userRepository.save(anotherUser);
        Long authorId = savedAnotherUser.getId();

        // When
        Page<Comment> comments = commentRepository.findByAuthorId(authorId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void findByAuthorId_ShouldReturnEmpty_WhenAuthorDoesNotExist() {
        // Given
        Long nonExistentAuthorId = 99999L;

        // When
        Page<Comment> comments = commentRepository.findByAuthorId(nonExistentAuthorId, PageRequest.of(0, 10));

        // Then
        assertThat(comments).isEmpty();
    }

    @Test
    void countByAuthorId_ShouldReturnCorrectCount_WhenAuthorHasComments() {
        // Given
        Long authorId = savedUser.getId();
        Comment comment1 = createComment("comment1", null, savedUser);
        Comment comment2 = createComment("comment2", null, savedUser);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // When
        Long count = commentRepository.countByAuthorId(authorId);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByAuthorId_ShouldReturnZero_WhenAuthorHasNoComments() {
        // Given
        User anotherUser = new User("anotherUser", "password");
        User savedAnotherUser = userRepository.save(anotherUser);
        Long authorId = savedAnotherUser.getId();

        // When
        Long count = commentRepository.countByAuthorId(authorId);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void countByAuthorId_ShouldReturnZero_WhenAuthorDoesNotExist() {
        // Given
        Long nonExistentAuthorId = 99999L;

        // When
        Long count = commentRepository.countByAuthorId(nonExistentAuthorId);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findMethods_ShouldHandlePagination() {
        // Given - Create multiple comments
        Long postId = savedPost.getId();
        for (int i = 1; i <= 5; i++) {
            Comment comment = createComment("comment" + i, savedPost, savedUser);
            commentRepository.save(comment);
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
    void countMethods_ShouldBeConsistent_WithFindMethods() {
        // Given
        Long postId = savedPost.getId();
        Long authorId = savedUser.getId();

        Comment comment = createComment(TEST_COMMENT_CONTENT, savedPost, savedUser);
        commentRepository.save(comment);

        // When
        Page<Comment> foundByPost = commentRepository.findByPostId(postId, PageRequest.of(0, 10));
        Long countByPost = commentRepository.countByPostId(postId);

        Page<Comment> foundByAuthor = commentRepository.findByAuthorId(authorId, PageRequest.of(0, 10));
        Long countByAuthor = commentRepository.countByAuthorId(authorId);

        // Then
        assertThat(foundByPost.getTotalElements()).isEqualTo(countByPost);
        assertThat(foundByAuthor.getTotalElements()).isEqualTo(countByAuthor);
    }

    private Comment createComment(String content, Post post, User author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        return comment;
    }
}
