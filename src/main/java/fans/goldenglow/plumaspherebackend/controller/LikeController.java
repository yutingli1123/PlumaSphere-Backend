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

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LikeController {
    private final LikeCacheService likeCacheService;
    private final WebSocketHandler webSocketHandler;
    private final CommentService commentService;

    @GetMapping("/post/{postId}/like")
    public ResponseEntity<Long> getLikes(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(likeCacheService.getPostLikesCount(postId));
    }

    @GetMapping("/comment/{commentId}/like")
    public ResponseEntity<Long> getCommentLikes(@PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(likeCacheService.getCommentLikesCount(commentId));
    }

    @GetMapping("/post/{postId}/like/state")
    public ResponseEntity<Boolean> getPostLikeState(@PathVariable("postId") Long postId, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return ResponseEntity.ok(likeCacheService.isPostLiked(postId, userId));
    }

    @GetMapping("/comment/{commentId}/like/state")
    public ResponseEntity<Boolean> getCommentLikeState(@PathVariable("commentId") Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        return ResponseEntity.ok(likeCacheService.isCommentLiked(commentId, userId));
    }

    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable("postId") Long postId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchPostLike(postId, userId);
        webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.LIKE_POST));

        return ResponseEntity.ok().build();
    }

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
