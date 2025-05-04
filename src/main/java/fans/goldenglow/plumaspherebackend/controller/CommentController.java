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
import java.util.stream.Collectors;

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
    public ResponseEntity<CommentDto> getComment(@PathVariable Long commentId) {
        Optional<Comment> comment = commentService.findById(commentId);
        if (comment.isEmpty()) return ResponseEntity.notFound().build();

        Comment commentEntity = comment.get();
        CommentDto commentDto = new CommentDto(commentEntity.getId(), commentEntity.getContent(), commentEntity.getCreatedAt(), commentEntity.getAuthor().getId());
        return ResponseEntity.ok(commentDto);
    }

    @GetMapping("/post/{postId}/comment")
    public ResponseEntity<Set<CommentDto>> getComments(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);
        if (post.isEmpty()) return ResponseEntity.notFound().build();

        Post postEntity = post.get();
        Set<Comment> comment = postEntity.getComments();
        Set<CommentDto> commentDtos = comment.stream().map(value -> new CommentDto(value.getId(), value.getContent(), value.getCreatedAt(), value.getAuthor().getId())).collect(Collectors.toSet());
        return ResponseEntity.ok(commentDtos);
    }

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<Void> addComment(@PathVariable Long postId, @RequestBody CommentDto commentDto, JwtAuthenticationToken token) {
        Optional<Post> post = postService.findById(postId);

        if (post.isEmpty()) return ResponseEntity.notFound().build();

        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) return ResponseEntity.notFound().build();

        Post postEntity = post.get();
        User userEntity = user.get();
        Comment comment = new Comment(commentDto.getContent(), LocalDateTime.now(), userEntity);
        postEntity.addComment(comment);
        postService.save(postEntity);
        return ResponseEntity.ok().build();
    }
}
