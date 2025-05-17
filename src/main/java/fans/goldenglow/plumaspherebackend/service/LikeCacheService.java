package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LikeCacheService {
    private static final String POST_LIKES_KEY = "post:like:";
    private static final String COMMENT_LIKES_KEY = "comment:like:";

    private final RedisService redisService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final LikeCacheService self;

    @Autowired
    public LikeCacheService(RedisService redisService, PostService postService, CommentService commentService, UserService userService, LikeCacheService likeCacheService) {
        this.redisService = redisService;
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
        this.self = likeCacheService;
    }

    public Set<Long> getPostLikes(long postId) {
        String key = POST_LIKES_KEY + postId;

        if (!redisService.exists(key)) {
            return self.loadPostLikesToRedis(postId);
        }

        return stringSetToLongSet(redisService.getSetMembers(key));
    }

    public Set<Long> getCommentLikes(long commentId) {
        String key = COMMENT_LIKES_KEY + commentId;

        if (!redisService.exists(key)) {
            return self.loadCommentLikesToRedis(commentId);
        }

        return stringSetToLongSet(redisService.getSetMembers(key));
    }

    public long getPostLikesCount(long postId) {
        String key = POST_LIKES_KEY + postId;

        if (!redisService.exists(key)) {
            return self.loadPostLikesToRedis(postId).size();
        }

        Long count = redisService.getSetSize(key);

        return count != null ? count : 0L;
    }

    public long getCommentLikesCount(Long commentId) {
        String key = COMMENT_LIKES_KEY + commentId;

        if (!redisService.exists(key)) {
            return self.loadCommentLikesToRedis(commentId).size();
        }

        Long count = redisService.getSetSize(key);

        return count != null ? count : 0L;
    }

    private Set<Long> stringSetToLongSet(Set<String> stringSet) {
        if (stringSet == null || stringSet.isEmpty()) return new HashSet<>();
        return stringSet.stream().map(Long::valueOf).collect(Collectors.toSet());
    }


    public void likePost(Long postId, Long userId) {
        String key = POST_LIKES_KEY + postId;
        redisService.addToSet(key, userId.toString());
    }

    public void likeComment(Long commentId, Long userId) {
        String key = COMMENT_LIKES_KEY + commentId;
        redisService.addToSet(key, userId.toString());
    }

    public void unlikePost(Long postId, Long userId) {
        String key = POST_LIKES_KEY + postId;
        redisService.removeFromSet(key, userId.toString());
    }

    public void unlikeComment(Long commentId, Long userId) {
        String key = COMMENT_LIKES_KEY + commentId;
        redisService.removeFromSet(key, userId.toString());
    }

    @Transactional(readOnly = true)
    protected Set<Long> loadPostLikesToRedis(Long postId) {
        String key = POST_LIKES_KEY + postId;
        return postService.findById(postId)
                .map(post -> saveUsersToRedis(key, post.getLikedBy()))
                .orElse(new HashSet<>());
    }

    @Transactional(readOnly = true)
    protected Set<Long> loadCommentLikesToRedis(Long commentId) {
        String key = COMMENT_LIKES_KEY + commentId;
        return commentService.findById(commentId)
                .map(comment -> saveUsersToRedis(key, comment.getLikedBy()))
                .orElse(new HashSet<>());
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
    public void syncLikesToDatabase() {
        syncPostLikes();
        syncCommentLikes();
    }

    @PreDestroy
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
            Post postEntity = post.get();

            if (userIds != null && !userIds.isEmpty()) {
                for (String userId : userIds) {
                    userService.findById(Long.valueOf(userId)).ifPresent(postEntity::addLikedBy);
                }
                postService.save(postEntity);
            }
        }
    }

    private void syncCommentLikes() {
        Set<String> keys = redisService.getKeys(COMMENT_LIKES_KEY + "*");
        for (String key : keys) {
            Long commentId = Long.valueOf(key.substring(COMMENT_LIKES_KEY.length()));
            Set<String> userIds = redisService.getSetMembers(key);

            Optional<Comment> comment = commentService.findById(commentId);
            if (comment.isEmpty()) continue;
            Comment commentEntity = comment.get();

            if (userIds != null && !userIds.isEmpty()) {
                for (String userId : userIds) {
                    userService.findById(Long.valueOf(userId)).ifPresent(commentEntity::addLikedBy);
                }
                commentService.save(commentEntity);
            }
        }
    }
}
