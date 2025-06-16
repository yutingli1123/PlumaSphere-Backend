package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeCacheServiceTest {
    private static final String POST_LIKES_KEY = "post:like:";
    private static final String COMMENT_LIKES_KEY = "comment:like:";
    private static final String POST_LIKES_LOADED = "post:likes:loaded";
    private static final String COMMENT_LIKES_LOADED = "comment:likes:loaded";

    @Mock
    private RedisService redisService;

    @Mock
    private PostService postService;

    @Mock
    private CommentService commentService;

    @Mock
    private UserService userService;

    private LikeCacheService likeCacheService;

    @BeforeEach
    void setUp() {
        likeCacheService = new LikeCacheService(redisService, postService, commentService, userService);
    }

    @Test
    void testGetPostLikes_WhenAlreadyLoaded_ShouldReturnFromRedis() {
        // Given
        Long postId = 1L;
        Set<String> redisLikes = Set.of("10", "20", "30");
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.getSetMembers(POST_LIKES_KEY + postId)).thenReturn(redisLikes);

        // When
        Set<Long> result = likeCacheService.getPostLikes(postId);

        // Then
        assertEquals(Set.of(10L, 20L, 30L), result);
        verify(redisService).existsInSet(POST_LIKES_LOADED, postId.toString());
        verify(redisService).getSetMembers(POST_LIKES_KEY + postId);
        verify(postService, never()).findById(anyLong());
    }

    @Test
    void testGetPostLikes_WhenNotLoaded_ShouldLoadFromDatabase() {
        // Given
        Long postId = 1L;
        Post post = createPostWithLikes(postId, Set.of(createUser(10L), createUser(20L)));
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(false);
        when(postService.findById(postId)).thenReturn(Optional.of(post));

        // When
        Set<Long> result = likeCacheService.getPostLikes(postId);

        // Then
        assertEquals(Set.of(10L, 20L), result);
        verify(redisService).addToSet(POST_LIKES_LOADED, postId.toString());
        verify(redisService).addToSet(POST_LIKES_KEY + postId, "10");
        verify(redisService).addToSet(POST_LIKES_KEY + postId, "20");
    }

    @Test
    void testGetPostLikes_WhenPostNotFound_ShouldReturnEmptySet() {
        // Given
        Long postId = 1L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(false);
        when(postService.findById(postId)).thenReturn(Optional.empty());

        // When
        Set<Long> result = likeCacheService.getPostLikes(postId);

        // Then
        assertTrue(result.isEmpty());
        verify(redisService).addToSet(POST_LIKES_LOADED, postId.toString());
    }

    @Test
    void testGetCommentLikes_WhenAlreadyLoaded_ShouldReturnFromRedis() {
        // Given
        Long commentId = 1L;
        Set<String> redisLikes = Set.of("15", "25");
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(true);
        when(redisService.getSetMembers(COMMENT_LIKES_KEY + commentId)).thenReturn(redisLikes);

        // When
        Set<Long> result = likeCacheService.getCommentLikes(commentId);

        // Then
        assertEquals(Set.of(15L, 25L), result);
        verify(redisService).existsInSet(COMMENT_LIKES_LOADED, commentId.toString());
        verify(redisService).getSetMembers(COMMENT_LIKES_KEY + commentId);
    }

    @Test
    void testGetCommentLikes_WhenNotLoaded_ShouldLoadFromDatabase() {
        // Given
        Long commentId = 1L;
        Comment comment = createCommentWithLikes(commentId, Set.of(createUser(15L), createUser(25L)));
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(false);
        when(commentService.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        Set<Long> result = likeCacheService.getCommentLikes(commentId);

        // Then
        assertEquals(Set.of(15L, 25L), result);
        verify(redisService).addToSet(COMMENT_LIKES_LOADED, commentId.toString());
    }

    @Test
    void testGetPostLikesCount_WhenAlreadyLoaded_ShouldReturnFromRedis() {
        // Given
        Long postId = 1L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.getSetSize(POST_LIKES_KEY + postId)).thenReturn(5L);

        // When
        long result = likeCacheService.getPostLikesCount(postId);

        // Then
        assertEquals(5L, result);
        verify(redisService).getSetSize(POST_LIKES_KEY + postId);
        verify(postService, never()).findById(anyLong());
    }

    @Test
    void testGetPostLikesCount_WhenNotLoaded_ShouldLoadAndReturnSize() {
        // Given
        Long postId = 1L;
        Post post = createPostWithLikes(postId, Set.of(createUser(10L), createUser(20L), createUser(30L)));
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(false);
        when(postService.findById(postId)).thenReturn(Optional.of(post));

        // When
        long result = likeCacheService.getPostLikesCount(postId);

        // Then
        assertEquals(3L, result);
        verify(postService).findById(postId);
    }

    @Test
    void testGetPostLikesCount_WhenRedisSizeIsNull_ShouldReturnZero() {
        // Given
        Long postId = 1L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.getSetSize(POST_LIKES_KEY + postId)).thenReturn(null);

        // When
        long result = likeCacheService.getPostLikesCount(postId);

        // Then
        assertEquals(0L, result);
    }

    @Test
    void testGetCommentLikesCount_WhenAlreadyLoaded_ShouldReturnFromRedis() {
        // Given
        Long commentId = 1L;
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(true);
        when(redisService.getSetSize(COMMENT_LIKES_KEY + commentId)).thenReturn(3L);

        // When
        long result = likeCacheService.getCommentLikesCount(commentId);

        // Then
        assertEquals(3L, result);
        verify(redisService).getSetSize(COMMENT_LIKES_KEY + commentId);
    }

    @Test
    void testSwitchPostLike_WhenUserNotLiked_ShouldAddLike() {
        // Given
        Long postId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.existsInSet(POST_LIKES_KEY + postId, userId.toString())).thenReturn(false);

        // When
        likeCacheService.switchPostLike(postId, userId);

        // Then
        verify(redisService).addToSet(POST_LIKES_KEY + postId, userId.toString());
        verify(redisService, never()).removeFromSet(anyString(), anyString());
    }

    @Test
    void testSwitchPostLike_WhenUserAlreadyLiked_ShouldRemoveLike() {
        // Given
        Long postId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.existsInSet(POST_LIKES_KEY + postId, userId.toString())).thenReturn(true);

        // When
        likeCacheService.switchPostLike(postId, userId);

        // Then
        verify(redisService).removeFromSet(POST_LIKES_KEY + postId, userId.toString());
        verify(redisService, never()).addToSet(anyString(), anyString());
    }

    @Test
    void testSwitchCommentLike_WhenUserNotLiked_ShouldAddLike() {
        // Given
        Long commentId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(true);
        when(redisService.existsInSet(COMMENT_LIKES_KEY + commentId, userId.toString())).thenReturn(false);

        // When
        likeCacheService.switchCommentLike(commentId, userId);

        // Then
        verify(redisService).addToSet(COMMENT_LIKES_KEY + commentId, userId.toString());
        verify(redisService, never()).removeFromSet(anyString(), anyString());
    }

    @Test
    void testSwitchCommentLike_WhenUserAlreadyLiked_ShouldRemoveLike() {
        // Given
        Long commentId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(true);
        when(redisService.existsInSet(COMMENT_LIKES_KEY + commentId, userId.toString())).thenReturn(true);

        // When
        likeCacheService.switchCommentLike(commentId, userId);

        // Then
        verify(redisService).removeFromSet(COMMENT_LIKES_KEY + commentId, userId.toString());
        verify(redisService, never()).addToSet(anyString(), anyString());
    }

    @Test
    void testIsPostLiked_WhenUserLiked_ShouldReturnTrue() {
        // Given
        Long postId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.existsInSet(POST_LIKES_KEY + postId, userId.toString())).thenReturn(true);

        // When
        boolean result = likeCacheService.isPostLiked(postId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsPostLiked_WhenUserNotLiked_ShouldReturnFalse() {
        // Given
        Long postId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.existsInSet(POST_LIKES_KEY + postId, userId.toString())).thenReturn(false);

        // When
        boolean result = likeCacheService.isPostLiked(postId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsCommentLiked_WhenUserLiked_ShouldReturnTrue() {
        // Given
        Long commentId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(true);
        when(redisService.existsInSet(COMMENT_LIKES_KEY + commentId, userId.toString())).thenReturn(true);

        // When
        boolean result = likeCacheService.isCommentLiked(commentId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsCommentLiked_WhenUserNotLiked_ShouldReturnFalse() {
        // Given
        Long commentId = 1L;
        Long userId = 10L;
        when(redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())).thenReturn(true);
        when(redisService.existsInSet(COMMENT_LIKES_KEY + commentId, userId.toString())).thenReturn(false);

        // When
        boolean result = likeCacheService.isCommentLiked(commentId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void testSyncLikesToDatabase_ShouldSyncPostsAndComments() {
        // Given
        Set<String> postKeys = Set.of(POST_LIKES_KEY + "1", POST_LIKES_KEY + "2");
        Set<String> commentKeys = Set.of(COMMENT_LIKES_KEY + "1", COMMENT_LIKES_KEY + "2");

        when(redisService.getKeys(POST_LIKES_KEY + "*")).thenReturn(postKeys);
        when(redisService.getKeys(COMMENT_LIKES_KEY + "*")).thenReturn(commentKeys);

        // Mock post sync
        when(redisService.getSetMembers(POST_LIKES_KEY + "1")).thenReturn(Set.of("10", "20"));
        when(redisService.getSetMembers(POST_LIKES_KEY + "2")).thenReturn(Set.of("30"));
        Post post1 = createPostWithLikes(1L, new HashSet<>());
        Post post2 = createPostWithLikes(2L, new HashSet<>());
        when(postService.findById(1L)).thenReturn(Optional.of(post1));
        when(postService.findById(2L)).thenReturn(Optional.of(post2));
        when(userService.findById(10L)).thenReturn(Optional.of(createUser(10L)));
        when(userService.findById(20L)).thenReturn(Optional.of(createUser(20L)));
        when(userService.findById(30L)).thenReturn(Optional.of(createUser(30L)));

        // Mock comment sync
        when(redisService.getSetMembers(COMMENT_LIKES_KEY + "1")).thenReturn(Set.of("40"));
        when(redisService.getSetMembers(COMMENT_LIKES_KEY + "2")).thenReturn(Set.of("50", "60"));
        Comment comment1 = createCommentWithLikes(1L, new HashSet<>());
        Comment comment2 = createCommentWithLikes(2L, new HashSet<>());
        when(commentService.findById(1L)).thenReturn(Optional.of(comment1));
        when(commentService.findById(2L)).thenReturn(Optional.of(comment2));
        when(userService.findById(40L)).thenReturn(Optional.of(createUser(40L)));
        when(userService.findById(50L)).thenReturn(Optional.of(createUser(50L)));
        when(userService.findById(60L)).thenReturn(Optional.of(createUser(60L)));

        // When
        likeCacheService.syncLikesToDatabase();

        // Then
        verify(postService).save(post1);
        verify(postService).save(post2);
        verify(commentService).save(comment1);
        verify(commentService).save(comment2);
    }

    @Test
    void testSyncLikesToDatabase_WhenEntityNotFound_ShouldSkip() {
        // Given
        Set<String> postKeys = Set.of(POST_LIKES_KEY + "999");
        when(redisService.getKeys(POST_LIKES_KEY + "*")).thenReturn(postKeys);
        when(redisService.getKeys(COMMENT_LIKES_KEY + "*")).thenReturn(Set.of());
        when(postService.findById(999L)).thenReturn(Optional.empty());

        // When
        likeCacheService.syncLikesToDatabase();

        // Then
        verify(postService, never()).save(any());
    }

    @Test
    void testDestroy_ShouldCallSyncLikesToDatabase() {
        // Given
        when(redisService.getKeys(anyString())).thenReturn(Set.of());

        // When
        likeCacheService.destroy();

        // Then
        verify(redisService, atLeastOnce()).getKeys(anyString());
    }

    @Test
    void testStringSetToLongSet_WithValidNumbers() {
        // This tests the private method through getPostLikes
        // Given
        Long postId = 1L;
        Set<String> redisLikes = Set.of("1", "2", "3");
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.getSetMembers(POST_LIKES_KEY + postId)).thenReturn(redisLikes);

        // When
        Set<Long> result = likeCacheService.getPostLikes(postId);

        // Then
        assertEquals(Set.of(1L, 2L, 3L), result);
    }

    @Test
    void testStringSetToLongSet_WithEmptySet() {
        // Given
        Long postId = 1L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.getSetMembers(POST_LIKES_KEY + postId)).thenReturn(Set.of());

        // When
        Set<Long> result = likeCacheService.getPostLikes(postId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testStringSetToLongSet_WithNullSet() {
        // Given
        Long postId = 1L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(true);
        when(redisService.getSetMembers(POST_LIKES_KEY + postId)).thenReturn(null);

        // When
        Set<Long> result = likeCacheService.getPostLikes(postId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoadPostLikesToRedis_EnsuresDataIsLoaded() {
        // Given
        Long postId = 1L;
        Long userId = 10L;
        Long otherUserId = 20L;
        when(redisService.existsInSet(POST_LIKES_LOADED, postId.toString())).thenReturn(false);
        // Post has multiple users who liked it
        Post post = createPostWithLikes(postId, Set.of(createUser(userId), createUser(otherUserId)));
        when(postService.findById(postId)).thenReturn(Optional.of(post));
        when(redisService.existsInSet(POST_LIKES_KEY + postId, userId.toString())).thenReturn(false);

        // When - First call should load data from database, then execute switch operation
        likeCacheService.switchPostLike(postId, userId);

        // Then - Verify loading operation occurred
        verify(redisService).addToSet(POST_LIKES_LOADED, postId.toString());
        // Verify data loading (one call for each user in database + one for switch operation)
        verify(redisService, times(2)).addToSet(eq(POST_LIKES_KEY + postId), eq(userId.toString()));
        verify(redisService).addToSet(eq(POST_LIKES_KEY + postId), eq(otherUserId.toString()));
        // Verify no remove operation (since user was not previously liked)
        verify(redisService, never()).removeFromSet(anyString(), anyString());
    }

    // Helper methods
    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        return user;
    }

    private Post createPostWithLikes(Long id, Set<User> likedBy) {
        Post post = new Post();
        post.setId(id);
        post.setTitle("Test Post " + id);
        post.setLikedBy(likedBy);
        return post;
    }

    private Comment createCommentWithLikes(Long id, Set<User> likedBy) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent("Test Comment " + id);
        comment.setLikedBy(likedBy);
        return comment;
    }
}
