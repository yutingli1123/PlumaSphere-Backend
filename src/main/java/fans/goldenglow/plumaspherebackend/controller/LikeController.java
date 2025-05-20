package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.WebSocketMessageType;
import fans.goldenglow.plumaspherebackend.dto.websocket.CommentLikeMessageDto;
import fans.goldenglow.plumaspherebackend.dto.websocket.WebSocketMessageDto;
import fans.goldenglow.plumaspherebackend.handler.WebSocketHandler;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.LikeCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LikeController {
    private final LikeCacheService likeCacheService;
    private final WebSocketHandler webSocketHandler;
    private final CommentService commentService;

    @Autowired
    public LikeController(LikeCacheService likeCacheService, WebSocketHandler webSocketHandler, CommentService commentService) {
        this.likeCacheService = likeCacheService;
        this.webSocketHandler = webSocketHandler;
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}/like")
    public ResponseEntity<Long> getLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(likeCacheService.getPostLikesCount(postId));
    }

    @GetMapping("/comment/{commentId}/like")
    public ResponseEntity<Long> getCommentLikes(@PathVariable Long commentId) {
        return ResponseEntity.ok(likeCacheService.getCommentLikesCount(commentId));
    }

    @GetMapping("/post/{postId}/like/state")
    public ResponseEntity<Boolean> getPostLikeState(@PathVariable Long postId, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return ResponseEntity.ok(likeCacheService.isPostLiked(postId, userId));
    }

    @GetMapping("/comment/{commentId}/like/state")
    public ResponseEntity<Boolean> getCommentLikeState(@PathVariable Long commentId, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return ResponseEntity.ok(likeCacheService.isCommentLiked(commentId, userId));
    }

    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchPostLike(postId, userId);
        webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.LIKE_POST));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchCommentLike(commentId, userId);

        Long postId = commentService.findPostId(commentId);
        if (postId != null) {
            webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.LIKE_COMMENT, new CommentLikeMessageDto(commentId)));
        }

        return ResponseEntity.ok().build();
    }
}
