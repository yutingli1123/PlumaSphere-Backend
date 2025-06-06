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

@Service
@RequiredArgsConstructor
public class LikeCacheService {
    private static final String POST_LIKES_KEY = "post:like:";
    private static final String COMMENT_LIKES_KEY = "comment:like:";
    private static final String POST_LIKES_LOADED = "post:likes:loaded";
    private static final String COMMENT_LIKES_LOADED = "comment:likes:loaded";

    private final RedisService redisService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Set<Long> getPostLikes(Long postId) {
        if (!redisService.existsInSet(POST_LIKES_LOADED, postId.toString())) {
            return loadPostLikesToRedis(postId);
        }

        String key = POST_LIKES_KEY + postId;
        return stringSetToLongSet(redisService.getSetMembers(key));
    }

    @Transactional(readOnly = true)
    public Set<Long> getCommentLikes(Long commentId) {
        if (!redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())) {
            return loadCommentLikesToRedis(commentId);
        }

        String key = COMMENT_LIKES_KEY + commentId;
        return stringSetToLongSet(redisService.getSetMembers(key));
    }

    @Transactional(readOnly = true)
    public long getPostLikesCount(Long postId) {
        if (!redisService.existsInSet(POST_LIKES_LOADED, postId.toString())) {
            return loadPostLikesToRedis(postId).size();
        }

        String key = POST_LIKES_KEY + postId;
        Long count = redisService.getSetSize(key);
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public long getCommentLikesCount(Long commentId) {
        if (!redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())) {
            return loadCommentLikesToRedis(commentId).size();
        }

        String key = COMMENT_LIKES_KEY + commentId;
        Long count = redisService.getSetSize(key);
        return count != null ? count : 0L;
    }

    private Set<Long> stringSetToLongSet(Set<String> stringSet) {
        if (stringSet == null || stringSet.isEmpty()) return new HashSet<>();
        return stringSet.stream().map(Long::valueOf).collect(Collectors.toSet());
    }

    public void switchPostLike(Long postId, Long userId) {
        ensurePostLikesLoaded(postId);

        String key = POST_LIKES_KEY + postId;
        if (redisService.existsInSet(key, userId.toString())) {
            redisService.removeFromSet(key, userId.toString());
        } else {
            redisService.addToSet(key, userId.toString());
        }
    }

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

    public boolean isPostLiked(Long postId, Long userId) {
        ensurePostLikesLoaded(postId);

        String key = POST_LIKES_KEY + postId;
        return redisService.existsInSet(key, userId.toString());
    }

    @Transactional(readOnly = true)
    public boolean isCommentLiked(Long commentId, Long userId) {
        ensureCommentLikesLoaded(commentId);

        String key = COMMENT_LIKES_KEY + commentId;
        return redisService.existsInSet(key, userId.toString());
    }

    private Set<Long> loadPostLikesToRedis(Long postId) {
        redisService.addToSet(POST_LIKES_LOADED, postId.toString());

        String key = POST_LIKES_KEY + postId;
        return postService.findById(postId)
                .map(post -> saveUsersToRedis(key, post.getLikedBy()))
                .orElse(new HashSet<>());
    }

    @Transactional(readOnly = true)
    protected Set<Long> loadCommentLikesToRedis(Long commentId) {
        redisService.addToSet(COMMENT_LIKES_LOADED, commentId.toString());

        String key = COMMENT_LIKES_KEY + commentId;
        return commentService.findById(commentId)
                .map(comment -> saveUsersToRedis(key, comment.getLikedBy()))
                .orElse(new HashSet<>());
    }

    private void ensurePostLikesLoaded(Long postId) {
        if (!redisService.existsInSet(POST_LIKES_LOADED, postId.toString())) {
            loadPostLikesToRedis(postId);
        }
    }

    @Transactional(readOnly = true)
    protected void ensureCommentLikesLoaded(Long commentId) {
        if (!redisService.existsInSet(COMMENT_LIKES_LOADED, commentId.toString())) {
            loadCommentLikesToRedis(commentId);
        }
    }

    private Set<Long> saveUsersToRedis(String key, Set<User> users) {
        Set<Long> userIds = new HashSet<>();
        users.forEach(user -> {
            redisService.addToSet(key, user.getId().toString());
            userIds.add(user.getId());
        });
        return userIds;
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void syncLikesToDatabase() {
        syncPostLikes();
        syncCommentLikes();
    }

    @PreDestroy
    @Transactional
    public void destroy() {
        syncLikesToDatabase();
    }

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

    private <T> void updateEntityLikes(T entity, Set<String> userIds, BiConsumer<T, Set<User>> setLikedByMethod, Consumer<T> saveMethod) {
        if (userIds != null && !userIds.isEmpty()) {
            Set<User> users = new HashSet<>();
            userIds.forEach(userId -> userService.findById(Long.valueOf(userId)).ifPresent(users::add));
            setLikedByMethod.accept(entity, users);
            saveMethod.accept(entity);
        }
    }
}
