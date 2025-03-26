package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.CommentDto;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class CommentController {
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @Autowired
    public CommentController(PostService postService, CommentService commentService, UserService userService, JWTUtil jwtUtil) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<Comment> getComment(@PathVariable Long commentId) {
        Optional<Comment> comment = commentService.findById(commentId);
        return comment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}/comment")
    public ResponseEntity<Set<Comment>> getComments(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);
        if (post.isPresent()) {
            Post postEntity = post.get();
            return ResponseEntity.ok(postEntity.getComments());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<Boolean> addComment(@PathVariable Long postId, @RequestBody CommentDto commentDto, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Optional<Post> post = postService.findById(postId);
        if (post.isPresent()) {
            String username = jwtUtil.getUsername(token);
            Optional<User> user = userService.findByUsername(username);
            if (user.isPresent()) {
                Post postEntity = post.get();
                User userEntity = user.get();
                Set<Comment> comments = postEntity.getComments();
                Comment comment = new Comment();
                comment.setAuthor(userEntity);
                comment.setContent(commentDto.getContent());
                comment.setCreatedAt(LocalDateTime.now());
                comments.add(comment);
                postEntity.setComments(comments);
                return ResponseEntity.ok(commentService.save(comment) && postService.save(postEntity));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
