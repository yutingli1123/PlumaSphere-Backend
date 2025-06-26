package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.WebSocketMessageType;
import fans.goldenglow.plumaspherebackend.dto.websocket.CommentLikeMessageDto;
import fans.goldenglow.plumaspherebackend.dto.websocket.WebSocketMessageDto;
import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.handler.WebSocketHandler;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.LikeCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for handling like operations on posts and comments.
 * Provides endpoints to get like counts, like states, and to toggle likes.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LikeController {
    private final LikeCacheService likeCacheService;
    private final WebSocketHandler webSocketHandler;
    private final CommentService commentService;

    /**
     * Retrieves the number of likes for a specific post.
     *
     * @param postId the ID of the post
     * @return the number of likes for the post
     */
    @GetMapping("/post/{postId}/like")
    public ResponseEntity<Long> getLikes(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(likeCacheService.getPostLikesCount(postId));
    }

    /**
     * Retrieves the number of likes for a specific comment.
     *
     * @param commentId the ID of the comment
     * @return the number of likes for the comment
     */
    @GetMapping("/comment/{commentId}/like")
    public ResponseEntity<Long> getCommentLikes(@PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(likeCacheService.getCommentLikesCount(commentId));
    }

    /**
     * Retrieves the like state of a post for the authenticated user.
     *
     * @param postId the ID of the post
     * @param token  the JWT authentication token of the user
     * @return if the post is liked by the user, false otherwise
     */
    @GetMapping("/post/{postId}/like/state")
    public ResponseEntity<Boolean> getPostLikeState(@PathVariable("postId") Long postId, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return ResponseEntity.ok(likeCacheService.isPostLiked(postId, userId));
    }

    /**
     * Retrieves the like state of a comment for the authenticated user.
     *
     * @param commentId the ID of the comment
     * @param token     the JWT authentication token of the user
     * @return if the comment is liked by the user, false otherwise
     */
    @GetMapping("/comment/{commentId}/like/state")
    public ResponseEntity<Boolean> getCommentLikeState(@PathVariable("commentId") Long commentId, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return ResponseEntity.ok(likeCacheService.isCommentLiked(commentId, userId));
    }

    /**
     * Toggles the like state of a post for the authenticated user.
     *
     * @param postId the ID of the post
     * @param token  the JWT authentication token of the user
     * @return HTTP 200 OK if successful
     */
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable("postId") Long postId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchPostLike(postId, userId);
        webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.LIKE_POST));

        return ResponseEntity.ok().build();
    }

    /**
     * Toggles the like state of a comment for the authenticated user.
     *
     * @param commentId the ID of the comment
     * @param token     the JWT authentication token of the user
     * @return HTTP 200 OK if successful
     */
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable("commentId") Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchCommentLike(commentId, userId);

        Long postId = commentService.findPostId(commentId);
        if (postId != null) {
            webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.LIKE_COMMENT, new CommentLikeMessageDto(commentId)));
        } else {
            Optional<Comment> comment = commentService.findById(commentId);
            if (comment.isPresent()) {
                Long parentId = comment.get().getParentComment().getId();
                webSocketHandler.sendMessageToComment(parentId, new WebSocketMessageDto(WebSocketMessageType.LIKE_COMMENT, new CommentLikeMessageDto(commentId)));
            }
        }

        return ResponseEntity.ok().build();
    }
}
