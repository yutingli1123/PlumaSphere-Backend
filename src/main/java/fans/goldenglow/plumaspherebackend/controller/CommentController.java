package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.annotation.CheckIpBan;
import fans.goldenglow.plumaspherebackend.annotation.CheckUserBan;
import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing comments on posts or reply on comments.
 * Provides endpoints for creating, retrieving, and deleting comments.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final WebSocketHandler webSocketHandler;
    private final CommentMapper commentMapper;
    private final ConfigService configService;
    private int pageSize;

    /**
     * Initializes the page size from the configuration service.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    void init() {
        Optional<String> pageSizeConfig = configService.get(ConfigField.PAGE_SIZE);
        pageSize = pageSizeConfig.map(Integer::parseInt).orElse(5);
    }

    /**
     * Retrieves a comment by its ID.
     *
     * @param commentId the ID of the comment to retrieve
     * @return ResponseEntity containing the CommentDto if found, or 404 Not Found if not found
     */
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable("commentId") Long commentId) {
        Optional<Comment> comment = commentService.findById(commentId);
        return comment.map(value -> ResponseEntity.ok(commentMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves comments for a specific post.
     *
     * @param postId the ID of the post to retrieve comments for
     * @param page   the page number to retrieve
     * @param sortBy the field to sort by (default is "time")
     * @return ResponseEntity containing a list of CommentDto objects
     */
    @GetMapping("/post/{postId}/comment")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable("postId") Long postId, @RequestParam int page, @RequestParam(required = false, defaultValue = "time") String sortBy) {
        String sortField = switch (sortBy) {
            case "like" -> "likedCount";
            case "time" -> "createdAt";
            default -> "createdAt";
        };
        Page<Comment> comments = commentService.findByPostId(postId, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, sortField)));
        return ResponseEntity.ok(commentMapper.toDto(comments.getContent()));
    }

    /**
     * Retrieves the total number of pages for comments on a specific post.
     *
     * @param postId the ID of the post to count comments for
     * @return ResponseEntity containing the total number of pages
     */
    @GetMapping("/post/{postId}/comment/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getCommentPageCount(@PathVariable("postId") Long postId) {
        long totalComments = commentService.countByPostId(postId);
        long totalPages = (long) Math.ceil((double) totalComments / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    /**
     * Retrieves the total number of comments for a specific post.
     *
     * @param postId the ID of the post to count comments for
     * @return ResponseEntity containing the total number of comments
     */
    @GetMapping("/post/{postId}/comment/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getCommentCount(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(commentService.countByPostId(postId));
    }

    /**
     * Retrieves comments made by a specific user.
     *
     * @param userId the ID of the user to retrieve comments for
     * @param page   the page number to retrieve
     * @return ResponseEntity containing a list of CommentDto objects
     */
    @GetMapping("/user/{userId}/comment")
    public ResponseEntity<List<CommentDto>> getUserComments(@PathVariable("userId") Long userId, @RequestParam int page) {
        Page<Comment> comments = commentService.findByUserId(userId, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(commentMapper.toDto(comments.getContent()));
    }

    /**
     * Retrieves the total number of comments made by a specific user.
     *
     * @param userId the ID of the user to count comments for
     * @return ResponseEntity containing the total number of comments
     */
    @GetMapping("/user/{userId}/comment/count")
    public ResponseEntity<Long> getUserCommentCount(@PathVariable("userId") Long userId) {
        long count = commentService.countByUserId(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Retrieves the total number of pages for comments made by a specific user.
     *
     * @param userId the ID of the user to count comments for
     * @return ResponseEntity containing the total number of pages
     */
    @GetMapping("/user/{userId}/comment/count-page")
    public ResponseEntity<Long> getUserCommentPageCount(@PathVariable("userId") Long userId) {
        long totalComments = commentService.countByUserId(userId);
        long totalPages = (long) Math.ceil((double) totalComments / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    /**
     * Replies to a post with a comment.
     *
     * @param postId     the ID of the post to reply to
     * @param commentDto the content of the comment
     * @param token      the JWT authentication token of the user
     * @return ResponseEntity indicating success or failure
     */
    @CheckIpBan
    @CheckUserBan
    @PostMapping("/post/{postId}/comment")
    @Transactional
    public ResponseEntity<Void> replyPost(@PathVariable("postId") Long postId, @RequestBody CommentDto commentDto, JwtAuthenticationToken token) {
        Optional<Post> post = postService.findById(postId);

        if (post.isEmpty()) return ResponseEntity.notFound().build();

        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) return ResponseEntity.notFound().build();

        Post postEntity = post.get();
        User userEntity = user.get();
        Comment comment = new Comment(commentDto.getContent(), userEntity);
        comment.setPost(postEntity);
        commentService.save(comment);

        // Notify WebSocket clients about the new comment
        webSocketHandler.sendMessageToPost(postId, new WebSocketMessageDto(WebSocketMessageType.NEW_COMMENT));

        return ResponseEntity.ok().build();
    }

    /**
     * Replies to a comment with a new comment.
     *
     * @param commentId  the ID of the comment to reply to
     * @param commentDto the content of the reply comment
     * @param token      the JWT authentication token of the user
     * @return ResponseEntity indicating success or failure
     */
    @CheckIpBan
    @CheckUserBan
    @PostMapping("/comment/{commentId}/reply")
    @Transactional
    public ResponseEntity<Void> replyComment(@PathVariable("commentId") Long commentId, @RequestBody CommentDto commentDto, JwtAuthenticationToken token) {
        Optional<Comment> comment = commentService.findById(commentId);
        if (comment.isEmpty()) return ResponseEntity.notFound().build();

        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();

        User userEntity = user.get();
        Comment commentEntity = comment.get();
        Comment newComment = new Comment(commentDto.getContent(), userEntity);
        newComment.setParentComment(commentEntity);
        commentService.save(newComment);

        Long parentCommentId = commentEntity.getId();

        // Notify WebSocket clients about the new reply comment
        webSocketHandler.sendMessageToComment(parentCommentId, new WebSocketMessageDto(WebSocketMessageType.NEW_COMMENT));
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves replies to a specific comment.
     *
     * @param commentId the ID of the comment to retrieve replies for
     * @param page the page number to retrieve
     * @param sortBy the field to sort by (default is "time")
     * @return ResponseEntity containing a list of CommentDto objects representing the replies
     */
    @GetMapping("/comment/{commentId}/reply")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentDto>> getCommentReplies(@PathVariable("commentId") Long commentId, @RequestParam int page, @RequestParam(required = false, defaultValue = "time") String sortBy) {
        String sortField = switch (sortBy) {
            case "like" -> "likedCount";
            case "time" -> "createdAt";
            default -> "createdAt";
        };
        Page<Comment> replies = commentService.findByParentCommentId(commentId, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, sortField)));
        return ResponseEntity.ok(commentMapper.toDto(replies.getContent()));
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param commentId the ID of the comment to delete
     * @param token     the JWT authentication token of the user
     * @return ResponseEntity indicating success or failure
     */
    @CheckUserBan
    @CheckIpBan
    @DeleteMapping("/comment/{commentId}")
    @Transactional
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Optional<Comment> comment = commentService.findById(commentId);
        if (comment.isEmpty()) return ResponseEntity.notFound().build();
        User userEntity = user.get();
        Comment commentEntity = comment.get();

        /* Check if the user is an admin or the author of the comment
         * Only admins or the author of the comment can delete it.
         */
        if (userEntity.getRole() != UserRoles.ADMIN && !commentEntity.getAuthor().equals(userEntity))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        commentService.delete(commentEntity);
        return ResponseEntity.ok().build();
    }
}
