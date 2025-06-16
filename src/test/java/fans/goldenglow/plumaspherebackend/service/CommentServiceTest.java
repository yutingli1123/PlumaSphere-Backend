package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private Post testPost;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test Comment");
        testComment.setAuthor(testUser);
        testComment.setPost(testPost);

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    void findById_ShouldReturnComment_WhenCommentExists() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When
        Optional<Comment> actualComment = commentService.findById(commentId);

        // Then
        assertThat(actualComment).isPresent();
        assertThat(actualComment.get()).isEqualTo(testComment);
        verify(commentRepository).findById(commentId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenCommentDoesNotExist() {
        // Given
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        Optional<Comment> actualComment = commentService.findById(commentId);

        // Then
        assertThat(actualComment).isEmpty();
        verify(commentRepository).findById(commentId);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void findById_ShouldHandleEdgeCaseIds(Long commentId) {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        Optional<Comment> actualComment = commentService.findById(commentId);

        // Then
        assertThat(actualComment).isEmpty();
        verify(commentRepository).findById(commentId);
    }

    @Test
    void findByPostId_ShouldReturnPageOfComments_WhenCommentsExist() {
        // Given
        Long postId = 1L;
        List<Comment> comments = List.of(testComment);
        Page<Comment> expectedPage = new PageImpl<>(comments, testPageable, 1);
        when(commentRepository.findByPostId(postId, testPageable)).thenReturn(expectedPage);

        // When
        Page<Comment> actualPage = commentService.findByPostId(postId, testPageable);

        // Then
        assertThat(actualPage).isEqualTo(expectedPage);
        assertThat(actualPage.getContent()).hasSize(1);
        assertThat(actualPage.getContent().getFirst()).isEqualTo(testComment);
        verify(commentRepository).findByPostId(postId, testPageable);
    }

    @Test
    void findByPostId_ShouldReturnEmptyPage_WhenNoCommentsExist() {
        // Given
        Long postId = 999L;
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(commentRepository.findByPostId(postId, testPageable)).thenReturn(emptyPage);

        // When
        Page<Comment> actualPage = commentService.findByPostId(postId, testPageable);

        // Then
        assertThat(actualPage).isEmpty();
        verify(commentRepository).findByPostId(postId, testPageable);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void findByPostId_ShouldHandleEdgeCasePostIds(Long postId) {
        // Given
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(commentRepository.findByPostId(postId, testPageable)).thenReturn(emptyPage);

        // When
        Page<Comment> actualPage = commentService.findByPostId(postId, testPageable);

        // Then
        assertThat(actualPage).isEmpty();
        verify(commentRepository).findByPostId(postId, testPageable);
    }

    @Test
    void findByUserId_ShouldReturnPageOfComments_WhenCommentsExist() {
        // Given
        Long userId = 1L;
        List<Comment> comments = List.of(testComment);
        Page<Comment> expectedPage = new PageImpl<>(comments, testPageable, 1);
        when(commentRepository.findByAuthorId(userId, testPageable)).thenReturn(expectedPage);

        // When
        Page<Comment> actualPage = commentService.findByUserId(userId, testPageable);

        // Then
        assertThat(actualPage).isEqualTo(expectedPage);
        assertThat(actualPage.getContent()).hasSize(1);
        assertThat(actualPage.getContent().getFirst()).isEqualTo(testComment);
        verify(commentRepository).findByAuthorId(userId, testPageable);
    }

    @Test
    void findByUserId_ShouldReturnEmptyPage_WhenNoCommentsExist() {
        // Given
        Long userId = 999L;
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(commentRepository.findByAuthorId(userId, testPageable)).thenReturn(emptyPage);

        // When
        Page<Comment> actualPage = commentService.findByUserId(userId, testPageable);

        // Then
        assertThat(actualPage).isEmpty();
        verify(commentRepository).findByAuthorId(userId, testPageable);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void findByUserId_ShouldHandleEdgeCaseUserIds(Long userId) {
        // Given
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(commentRepository.findByAuthorId(userId, testPageable)).thenReturn(emptyPage);

        // When
        Page<Comment> actualPage = commentService.findByUserId(userId, testPageable);

        // Then
        assertThat(actualPage).isEmpty();
        verify(commentRepository).findByAuthorId(userId, testPageable);
    }

    @Test
    void countByUserId_ShouldReturnCount_WhenCommentsExist() {
        // Given
        Long userId = 1L;
        Long expectedCount = 5L;
        when(commentRepository.countByAuthorId(userId)).thenReturn(expectedCount);

        // When
        Long actualCount = commentService.countByUserId(userId);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(commentRepository).countByAuthorId(userId);
    }

    @Test
    void countByUserId_ShouldReturnZero_WhenNoCommentsExist() {
        // Given
        Long userId = 999L;
        when(commentRepository.countByAuthorId(userId)).thenReturn(0L);

        // When
        Long actualCount = commentService.countByUserId(userId);

        // Then
        assertThat(actualCount).isZero();
        verify(commentRepository).countByAuthorId(userId);
    }

    @Test
    void countByPostId_ShouldReturnCount_WhenCommentsExist() {
        // Given
        Long postId = 1L;
        long expectedCount = 3L;
        when(commentRepository.countByPostId(postId)).thenReturn(expectedCount);

        // When
        long actualCount = commentService.countByPostId(postId);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(commentRepository).countByPostId(postId);
    }

    @Test
    void countByPostId_ShouldReturnZero_WhenNoCommentsExist() {
        // Given
        Long postId = 999L;
        when(commentRepository.countByPostId(postId)).thenReturn(0L);

        // When
        long actualCount = commentService.countByPostId(postId);

        // Then
        assertThat(actualCount).isZero();
        verify(commentRepository).countByPostId(postId);
    }

    @Test
    void findPostId_ShouldReturnPostId_WhenCommentExists() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When
        Long actualPostId = commentService.findPostId(commentId);

        // Then
        assertThat(actualPostId).isEqualTo(testPost.getId());
        verify(commentRepository).findById(commentId);
    }

    @Test
    void findPostId_ShouldReturnNull_WhenCommentDoesNotExist() {
        // Given
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        Long actualPostId = commentService.findPostId(commentId);

        // Then
        assertThat(actualPostId).isNull();
        verify(commentRepository).findById(commentId);
    }

    @Test
    void findPostId_ShouldReturnNull_WhenCommentHasNoPost() {
        // Given
        Long commentId = 1L;
        Comment commentWithoutPost = new Comment();
        commentWithoutPost.setId(commentId);
        commentWithoutPost.setPost(null);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentWithoutPost));

        // When
        Long actualPostId = commentService.findPostId(commentId);

        // Then
        assertThat(actualPostId).isNull();
        verify(commentRepository).findById(commentId);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void findPostId_ShouldHandleEdgeCaseCommentIds(Long commentId) {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        Long actualPostId = commentService.findPostId(commentId);

        // Then
        assertThat(actualPostId).isNull();
        verify(commentRepository).findById(commentId);
    }

    @Test
    void save_ShouldCallRepositorySave_WithGivenComment() {
        // When
        commentService.save(testComment);

        // Then
        verify(commentRepository).save(testComment);
    }

    @Test
    void findByParentCommentId_ShouldReturnPageOfComments_WhenChildCommentsExist() {
        // Given
        Long parentCommentId = 1L;
        Comment childComment = new Comment();
        childComment.setId(2L);
        childComment.setContent("Child Comment");
        childComment.setParentComment(testComment);

        List<Comment> childComments = List.of(childComment);
        Page<Comment> expectedPage = new PageImpl<>(childComments, testPageable, 1);
        when(commentRepository.findByParentCommentId(parentCommentId, testPageable)).thenReturn(expectedPage);

        // When
        Page<Comment> actualPage = commentService.findByParentCommentId(parentCommentId, testPageable);

        // Then
        assertThat(actualPage).isEqualTo(expectedPage);
        assertThat(actualPage.getContent()).hasSize(1);
        assertThat(actualPage.getContent().getFirst()).isEqualTo(childComment);
        verify(commentRepository).findByParentCommentId(parentCommentId, testPageable);
    }

    @Test
    void findByParentCommentId_ShouldReturnEmptyPage_WhenNoChildCommentsExist() {
        // Given
        Long parentCommentId = 999L;
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(commentRepository.findByParentCommentId(parentCommentId, testPageable)).thenReturn(emptyPage);

        // When
        Page<Comment> actualPage = commentService.findByParentCommentId(parentCommentId, testPageable);

        // Then
        assertThat(actualPage).isEmpty();
        verify(commentRepository).findByParentCommentId(parentCommentId, testPageable);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void findByParentCommentId_ShouldHandleEdgeCaseParentCommentIds(Long parentCommentId) {
        // Given
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(commentRepository.findByParentCommentId(parentCommentId, testPageable)).thenReturn(emptyPage);

        // When
        Page<Comment> actualPage = commentService.findByParentCommentId(parentCommentId, testPageable);

        // Then
        assertThat(actualPage).isEmpty();
        verify(commentRepository).findByParentCommentId(parentCommentId, testPageable);
    }

    @Test
    void delete_ShouldCallRepositoryDelete_WithGivenComment() {
        // When
        commentService.delete(testComment);

        // Then
        verify(commentRepository).delete(testComment);
    }

    @Test
    void delete_ShouldCallRepositoryDelete_WithCommentHavingChildComments() {
        // Given
        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setContent("Parent Comment");

        // When
        commentService.delete(parentComment);

        // Then
        verify(commentRepository).delete(parentComment);
    }
}
