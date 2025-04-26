package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LikeController {
    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;

    @Autowired
    public LikeController(PostService postService, UserService userService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}/like")
    public ResponseEntity<Set<User>> getLikes(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);
        return post.map(value -> ResponseEntity.ok(value.getLikedBy())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Void> postLike(@PathVariable Long postId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) return ResponseEntity.notFound().build();

        User userEntity = user.get();
        Optional<Post> post = postService.findById(postId);

        if (post.isEmpty()) return ResponseEntity.notFound().build();

        Post postEntity = post.get();
        postEntity.addLikedBy(userEntity);
        postService.save(postEntity);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Void> commentLike(@PathVariable Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) return ResponseEntity.notFound().build();

        User userEntity = user.get();
        Optional<Comment> comment = commentService.findById(commentId);

        if (comment.isEmpty()) return ResponseEntity.notFound().build();

        Comment commentEntity = comment.get();
        commentEntity.addLikedBy(userEntity);
        commentService.save(commentEntity);

        return ResponseEntity.ok().build();
    }
}
