package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import fans.goldenglow.plumaspherebackend.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LikeController {
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/post/{postId}/like")
    public ResponseEntity<Set<User>> getLikes(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);
        if (post.isPresent()) {
            Post postEntity = post.get();
            return ResponseEntity.ok(postEntity.getLikedBy());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Boolean> postLike(@PathVariable Long postId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        String username = jwtUtil.getUsername(token);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            User userEntity = user.get();
            Optional<Post> post = postService.findById(postId);
            if (post.isPresent()) {
                Post postEntity = post.get();
                Set<User> users = postEntity.getLikedBy();
                users.add(userEntity);
                return ResponseEntity.ok(postService.save(postEntity));
            }
        }
        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Boolean> commentLike(@PathVariable Long commentId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        String username = jwtUtil.getUsername(token);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            User userEntity = user.get();
            Optional<Comment> comment = commentService.findById(commentId);
            if (comment.isPresent()) {
                Comment commentEntity = comment.get();
                Set<User> users = commentEntity.getLikedBy();
                users.add(userEntity);
                return ResponseEntity.ok(commentService.save(commentEntity));
            }
        }
        return ResponseEntity.internalServerError().build();
    }
}
