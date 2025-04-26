package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.CommentDto;
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

    @Autowired
    public CommentController(PostService postService, CommentService commentService, UserService userService) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
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

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<Boolean> addComment(@PathVariable Long postId, @RequestBody CommentDto commentDto, JwtAuthenticationToken token) {
        Optional<Post> post = postService.findById(postId);
        if (post.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
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
