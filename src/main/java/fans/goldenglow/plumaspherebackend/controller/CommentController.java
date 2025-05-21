package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.constant.WebSocketMessageType;
import fans.goldenglow.plumaspherebackend.dto.CommentDto;
import fans.goldenglow.plumaspherebackend.dto.websocket.WebSocketMessageDto;
import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.handler.WebSocketHandler;
import fans.goldenglow.plumaspherebackend.mapper.CommentMapper;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class CommentController {
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final WebSocketHandler webSocketHandler;
    private final CommentMapper commentMapper;
    private final int pageSize;

    @Autowired
    public CommentController(PostService postService, CommentService commentService, UserService userService, WebSocketHandler webSocketHandler, CommentMapper commentMapper, ConfigService configService) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
        this.webSocketHandler = webSocketHandler;
        this.commentMapper = commentMapper;
        Optional<String> pageSizeConfig = configService.get(ConfigField.PAGE_SIZE);
        pageSize = pageSizeConfig.map(Integer::parseInt).orElse(5);
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long commentId) {
        Optional<Comment> comment = commentService.findById(commentId);
        return comment.map(value -> ResponseEntity.ok(commentMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}/comment")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId, @RequestParam int page) {
        Page<Comment> comments = commentService.findByPostId(postId, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(commentMapper.toDto(comments.getContent()));
    }

    @GetMapping("/post/{postId}/comment/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getCommentPageCount(@PathVariable Long postId) {
        long totalComments = commentService.countByPostId(postId);
        long totalPages = (long) Math.ceil((double) totalComments / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    @GetMapping("/post/{postId}/comment/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.countByPostId(postId));
    }

    @PostMapping("/post/{postId}/comment")
    @Transactional
    public ResponseEntity<Void> replyPost(@PathVariable Long postId, @RequestBody CommentDto commentDto, JwtAuthenticationToken token) {
        Optional<Post> post = postService.findById(postId);

        if (post.isEmpty()) return ResponseEntity.notFound().build();

        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) return ResponseEntity.notFound().build();

        Post postEntity = post.get();
        User userEntity = user.get();
        Comment comment = new Comment(commentDto.getContent(), userEntity);
        postEntity.addComment(comment);
        postService.save(postEntity);

        webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.NEW_COMMENT));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment/{commentId}/reply")
    @Transactional
    public ResponseEntity<Void> replyComment(@PathVariable Long commentId, @RequestBody CommentDto commentDto, JwtAuthenticationToken token) {
        Optional<Comment> comment = commentService.findById(commentId);
        if (comment.isEmpty()) return ResponseEntity.notFound().build();

        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();

        Comment commentEntity = comment.get();
        Comment newComment = new Comment(commentDto.getContent(), user.get());
        newComment.setParentComment(commentEntity);
        commentEntity.addComment(newComment);

//        webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.UPDATE_COMMENT));
        commentService.save(commentEntity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comment/{commentId}/reply")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentDto>> getCommentReplies(@PathVariable Long commentId, @RequestParam int page) {
        Page<Comment> replies = commentService.findByParentCommentId(commentId, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(commentMapper.toDto(replies.getContent()));
    }
}
