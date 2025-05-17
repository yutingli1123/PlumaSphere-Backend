package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.service.LikeCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LikeController {
    private final LikeCacheService likeCacheService;

    @Autowired
    public LikeController(LikeCacheService likeCacheService) {
        this.likeCacheService = likeCacheService;
    }

    @GetMapping("/post/{postId}/like")
    public ResponseEntity<Long> getLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(likeCacheService.getPostLikesCount(postId));
    }

    @GetMapping("/comment/{commentId}/like")
    public ResponseEntity<Long> getCommentLikes(@PathVariable Long commentId) {
        return ResponseEntity.ok(likeCacheService.getCommentLikesCount(commentId));
    }

    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.likePost(postId, userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.likeComment(commentId, userId);

        return ResponseEntity.ok().build();
    }
}
