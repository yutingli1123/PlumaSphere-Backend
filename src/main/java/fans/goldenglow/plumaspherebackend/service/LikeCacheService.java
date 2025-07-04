package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service for managing likes on posts and comments, including caching likes in Redis.
 * Provides methods to get, switch, and check likes, as well as syncing likes with the database.
 */
@Service
@RequiredArgsConstructor
public class LikeCacheService {
    // Redis keys for storing likes
    private static final String POST_LIKES_KEY = "post:like:";
    private static final String COMMENT_LIKES_KEY = "comment:like:";
    private static final String POST_LIKES_LOADED = "post:likes:loaded";
    private static final String COMMENT_LIKES_LOADED = "comment:likes:loaded";

    private final RedisService redisService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    /**
     * Retrieves the set of user IDs who liked a post.
     * If the likes are not loaded in Redis, it loads them from the database.
     *
     * @param postId the ID of the post
     * @return a set of user IDs who liked the post
     */
    @Transactional(readOnly = true)
    public Set<Long> getPostLikes(Long postId) {
        if (!redisService.existsInSet(POST_LIKES_LOADED, postId.toString())) {
            return loadPostLikesToRedis(postId);
        }

        String key = POST_LIKES_KEY + postId;
        return stringSetToLongSet(redisService.getSetMembers(key));
    }

    /**
     * Retrieves the set of user IDs who liked a comment.
     * If the likes are not loaded in Redis, it loads them from the database.
     *
     * @param commentId the ID of the comment
     * @return a set of user IDs who liked the comment
     */
    @Transactional(readOnly = true)
    public Set<Long> getCommentLikes(Long commentId) {
        if (!redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())) {
            return loadCommentLikesToRedis(commentId);
        }

        String key = COMMENT_LIKES_KEY + commentId;
        return stringSetToLongSet(redisService.getSetMembers(key));
    }

    /**
     * Retrieves the count of likes for a post.
     * If the likes are not loaded in Redis, it loads them from the database.
     *
     * @param postId the ID of the post
     * @return the count of likes for the post
     */
    @Transactional(readOnly = true)
    public long getPostLikesCount(Long postId) {
        if (!redisService.existsInSet(POST_LIKES_LOADED, postId.toString())) {
            return loadPostLikesToRedis(postId).size();
        }

        String key = POST_LIKES_KEY + postId;
        Long count = redisService.getSetSize(key);
        return count != null ? count : 0L;
    }

    /**
     * Retrieves the count of likes for a comment.
     * If the likes are not loaded in Redis, it loads them from the database.
     *
     * @param commentId the ID of the comment
     * @return the count of likes for the comment
     */
    @Transactional(readOnly = true)
    public long getCommentLikesCount(Long commentId) {
        if (!redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())) {
            return loadCommentLikesToRedis(commentId).size();
        }

        String key = COMMENT_LIKES_KEY + commentId;
        Long count = redisService.getSetSize(key);
        return count != null ? count : 0L;
    }

    /**
     * Converts a set of strings to a set of longs.
     * If the input set is null or empty, it returns an empty set.
     *
     * @param stringSet the set of strings to convert
     * @return a set of longs converted from the input set
     */
    private Set<Long> stringSetToLongSet(Set<String> stringSet) {
        if (stringSet == null || stringSet.isEmpty()) return new HashSet<>();
        return stringSet.stream().map(Long::valueOf).collect(Collectors.toSet());
    }

    /**
     * Switches the like status of a post for a user.
     * If the user has already liked the post, it removes the like; otherwise, it adds the like.
     *
     * @param postId the ID of the post
     * @param userId the ID of the user
     */
    public void switchPostLike(Long postId, Long userId) {
        ensurePostLikesLoaded(postId);

        String key = POST_LIKES_KEY + postId;
        if (redisService.existsInSet(key, userId.toString())) {
            redisService.removeFromSet(key, userId.toString());
        } else {
            redisService.addToSet(key, userId.toString());
        }
    }

    /**
     * Switches the like status of a comment for a user.
     * If the user has already liked the comment, it removes the like; otherwise, it adds the like.
     *
     * @param commentId the ID of the comment
     * @param userId    the ID of the user
     */
    @Transactional(readOnly = true)
    public void switchCommentLike(Long commentId, Long userId) {
        ensureCommentLikesLoaded(commentId);

        String key = COMMENT_LIKES_KEY + commentId;
        if (redisService.existsInSet(key, userId.toString())) {
            redisService.removeFromSet(key, userId.toString());
        } else {
            redisService.addToSet(key, userId.toString());
        }
    }

    /**
     * Checks if a post is liked by a user.
     * If the likes are not loaded in Redis, it loads them from the database.
     *
     * @param postId the ID of the post
     * @param userId the ID of the user
     * @return true if the post is liked by the user, false otherwise
     */
    public boolean isPostLiked(Long postId, Long userId) {
        ensurePostLikesLoaded(postId);

        String key = POST_LIKES_KEY + postId;
        return redisService.existsInSet(key, userId.toString());
    }

    /**
     * Checks if a comment is liked by a user.
     * If the likes are not loaded in Redis, it loads them from the database.
     *
     * @param commentId the ID of the comment
     * @param userId    the ID of the user
     * @return true if the comment is liked by the user, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isCommentLiked(Long commentId, Long userId) {
        ensureCommentLikesLoaded(commentId);

        String key = COMMENT_LIKES_KEY + commentId;
        return redisService.existsInSet(key, userId.toString());
    }

    /**
     * Loads post likes into Redis if they are not already loaded.
     * It retrieves the liked users from the database and saves them in Redis.
     *
     * @param postId the ID of the post
     * @return a set of user IDs who liked the post
     */
    private Set<Long> loadPostLikesToRedis(Long postId) {
        redisService.addToSet(POST_LIKES_LOADED, postId.toString());

        String key = POST_LIKES_KEY + postId;
        return postService.findById(postId)
                .map(post -> saveUsersToRedis(key, post.getLikedBy()))
                .orElse(new HashSet<>());
    }

    /**
     * Loads comment likes into Redis if they are not already loaded.
     * It retrieves the liked users from the database and saves them in Redis.
     *
     * @param commentId the ID of the comment
     * @return a set of user IDs who liked the comment
     */
    @Transactional(readOnly = true)
    protected Set<Long> loadCommentLikesToRedis(Long commentId) {
        redisService.addToSet(COMMENT_LIKES_LOADED, commentId.toString());

        String key = COMMENT_LIKES_KEY + commentId;
        return commentService.findById(commentId)
                .map(comment -> saveUsersToRedis(key, comment.getLikedBy()))
                .orElse(new HashSet<>());
    }

    /**
     * Ensures that post likes are loaded into Redis.
     * If they are not loaded, it loads them from the database.
     *
     * @param postId the ID of the post
     */
    private void ensurePostLikesLoaded(Long postId) {
        if (!redisService.existsInSet(POST_LIKES_LOADED, postId.toString())) {
            loadPostLikesToRedis(postId);
        }
    }

    /**
     * Ensures that comment likes are loaded into Redis.
     * If they are not loaded, it loads them from the database.
     *
     * @param commentId the ID of the comment
     */
    @Transactional(readOnly = true)
    protected void ensureCommentLikesLoaded(Long commentId) {
        if (!redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())) {
            loadCommentLikesToRedis(commentId);
        }
    }

    /**
     * Saves the users who liked a post or comment to Redis.
     * It adds each user's ID to the specified Redis set and returns the set of user IDs.
     *
     * @param key   the Redis key for the likes
     * @param users the set of users who liked the post or comment
     * @return a set of user IDs who liked the post or comment
     */
    private Set<Long> saveUsersToRedis(String key, Set<User> users) {
        Set<Long> userIds = new HashSet<>();
        users.forEach(user -> {
            redisService.addToSet(key, user.getId().toString());
            userIds.add(user.getId());
        });
        return userIds;
    }

    /**
     * Synchronizes likes from Redis to the database at configurable intervals.
     * This method is scheduled to run periodically to ensure that likes are consistent between Redis and the database.
     * The sync interval is configurable via config.cache.like_sync_interval in application.yml
     */
    @Scheduled(fixedRateString = "${config.cache.like_sync_interval}")
    @Transactional
    public void syncLikesToDatabase() {
        syncPostLikes();
        syncCommentLikes();
    }

    /**
     * Destroys the service by synchronizing likes to the database before shutdown.
     * This method is called when the application context is closed.
     */
    @PreDestroy
    @Transactional
    public void destroy() {
        syncLikesToDatabase();
    }

    /**
     * Synchronizes post likes from Redis to the database.
     * It retrieves all post likes from Redis and updates the corresponding Post entities in the database.
     */
    private void syncPostLikes() {
        Set<String> keys = redisService.getKeys(POST_LIKES_KEY + "*");
        for (String key : keys) {
            Long postId = Long.valueOf(key.substring(POST_LIKES_KEY.length()));
            Set<String> userIds = redisService.getSetMembers(key);

            Optional<Post> post = postService.findById(postId);
            if (post.isEmpty()) continue;

            updateEntityLikes(post.get(), userIds, Post::setLikedBy, postService::save);
        }
    }

    /**
     * Synchronizes comment likes from Redis to the database.
     * It retrieves all comment likes from Redis and updates the corresponding Comment entities in the database.
     */
    private void syncCommentLikes() {
        Set<String> keys = redisService.getKeys(COMMENT_LIKES_KEY + "*");
        for (String key : keys) {
            Long commentId = Long.valueOf(key.substring(COMMENT_LIKES_KEY.length()));
            Set<String> userIds = redisService.getSetMembers(key);

            Optional<Comment> comment = commentService.findById(commentId);
            if (comment.isEmpty()) continue;

            updateEntityLikes(comment.get(), userIds, Comment::setLikedBy, commentService::save);
        }
    }

    /**
     * Updates the likes of a Post or Comment entity based on the provided user IDs.
     * It sets the likedBy field and saves the entity to the database.
     *
     * @param entity           the Post or Comment entity to update
     * @param userIds          the set of user IDs who liked the entity
     * @param setLikedByMethod a method reference to set the likedBy field
     * @param saveMethod       a method reference to save the entity
     * @param <T>              the type of the entity (Post or Comment)
     */
    private <T> void updateEntityLikes(T entity, Set<String> userIds, BiConsumer<T, Set<User>> setLikedByMethod, Consumer<T> saveMethod) {
        if (userIds != null && !userIds.isEmpty()) {
            Set<User> users = new HashSet<>();
            userIds.forEach(userId -> userService.findById(Long.valueOf(userId)).ifPresent(users::add));
            setLikedByMethod.accept(entity, users);
            saveMethod.accept(entity);
        }
    }
}
