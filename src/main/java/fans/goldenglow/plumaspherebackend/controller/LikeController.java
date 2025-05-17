package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.LikeDto;
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
    public ResponseEntity<LikeDto> likePost(@PathVariable Long postId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchPostLike(postId, userId);

        Long count = likeCacheService.getPostLikesCount(postId);
        boolean isLiked = likeCacheService.isPostLiked(postId, userId);

        return ResponseEntity.ok(new LikeDto(count, isLiked));
    }

    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<LikeDto> likeComment(@PathVariable Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        likeCacheService.switchCommentLike(commentId, userId);

        Long count = likeCacheService.getCommentLikesCount(commentId);
        boolean isLiked = likeCacheService.isCommentLiked(commentId, userId);

        return ResponseEntity.ok(new LikeDto(count, isLiked));
    }
}
