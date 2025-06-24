package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.dto.CommentDto;
import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.handler.WebSocketHandler;
import fans.goldenglow.plumaspherebackend.mapper.CommentMapper;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("CommentController Tests")
class CommentControllerTest {
    @Mock
    private PostService postService;
    @Mock
    private CommentService commentService;
    @Mock
    private UserService userService;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ConfigService configService;
    @Mock
    private WebSocketHandler webSocketHandler;
    @InjectMocks
    private CommentController commentController;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // mock pageSize config for controller
        when(configService.get(ConfigField.PAGE_SIZE)).thenReturn(Optional.of("5"));
        commentController.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    private JwtAuthenticationToken createJwtToken(String userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .headers(h -> h.put("typ", "JWT"))
                .subject(userId)
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    @Nested
    @DisplayName("GET /comment/{commentId}")
    class GetComment {
        @Test
        @DisplayName("Should return comment by id")
        void getComment_ShouldReturnComment() {
            Comment comment = new Comment();
            CommentDto dto = new CommentDto();
            when(commentService.findById(1L)).thenReturn(Optional.of(comment));
            when(commentMapper.toDto(comment)).thenReturn(dto);
            ResponseEntity<CommentDto> response = commentController.getComment(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(dto);
        }

        @Test
        @DisplayName("Should return NOT_FOUND if comment does not exist")
        void getComment_ShouldReturnNotFound() {
            when(commentService.findById(1L)).thenReturn(Optional.empty());
            ResponseEntity<CommentDto> response = commentController.getComment(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /post/{postId}/comment")
    class ReplyPost {
        @Test
        @DisplayName("Should succeed when replying to post")
        void replyPost_ShouldSucceed() {
            CommentDto dto = new CommentDto();
            dto.setContent("test");
            Post post = new Post();
            User user = new User();
            user.setId(10L);
            JwtAuthenticationToken token = createJwtToken("10");
            when(postService.findById(1L)).thenReturn(Optional.of(post));
            when(userService.findById(10L)).thenReturn(Optional.of(user));
            ResponseEntity<Void> response = commentController.replyPost(1L, dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when post not found")
        void replyPost_PostNotFound() {
            CommentDto dto = new CommentDto();
            JwtAuthenticationToken token = createJwtToken("10");
            when(postService.findById(1L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = commentController.replyPost(1L, dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user not found")
        void replyPost_UserNotFound() {
            CommentDto dto = new CommentDto();
            Post post = new Post();
            JwtAuthenticationToken token = createJwtToken("10");
            when(postService.findById(1L)).thenReturn(Optional.of(post));
            when(userService.findById(10L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = commentController.replyPost(1L, dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /comment/{commentId}/reply")
    class ReplyComment {
        @Test
        @DisplayName("Should succeed when replying to comment")
        void replyComment_ShouldSucceed() {
            CommentDto dto = new CommentDto();
            dto.setContent("reply");
            Comment parent = new Comment();
            parent.setId(2L);
            User user = new User();
            user.setId(10L);
            JwtAuthenticationToken token = createJwtToken("10");
            when(commentService.findById(2L)).thenReturn(Optional.of(parent));
            when(userService.findById(10L)).thenReturn(Optional.of(user));
            ResponseEntity<Void> response = commentController.replyComment(2L, dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when comment not found")
        void replyComment_CommentNotFound() {
            CommentDto dto = new CommentDto();
            JwtAuthenticationToken token = createJwtToken("10");
            when(commentService.findById(2L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = commentController.replyComment(2L, dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user not found")
        void replyComment_UserNotFound() {
            CommentDto dto = new CommentDto();
            Comment parent = new Comment();
            parent.setId(2L);
            JwtAuthenticationToken token = createJwtToken("10");
            when(commentService.findById(2L)).thenReturn(Optional.of(parent));
            when(userService.findById(10L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = commentController.replyComment(2L, dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /comment/{commentId}/reply")
    class GetCommentReplies {
        @Test
        @DisplayName("Should return replies for comment")
        void getCommentReplies_ShouldReturnReplies() {
            Comment reply = new Comment();
            CommentDto dto = new CommentDto();
            Page<Comment> page = new PageImpl<>(List.of(reply));
            when(commentService.findByParentCommentId(eq(3L), any())).thenReturn(page);
            when(commentMapper.toDto(List.of(reply))).thenReturn(List.of(dto));
            ResponseEntity<List<CommentDto>> response = commentController.getCommentReplies(3L, 0, "time");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsExactly(dto);
        }

        @Test
        @DisplayName("Should return empty list when no replies")
        void getCommentReplies_ShouldReturnEmptyList() {
            Page<Comment> page = new PageImpl<>(Collections.emptyList());
            when(commentService.findByParentCommentId(eq(3L), any())).thenReturn(page);
            when(commentMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());
            ResponseEntity<List<CommentDto>> response = commentController.getCommentReplies(3L, 0, "like");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("DELETE /comment/{commentId}")
    class DeleteComment {
        @Test
        @DisplayName("Should succeed for author with REGULAR role")
        void deleteComment_ShouldSucceedForAuthor() {
            User user = new User();
            user.setId(10L);
            user.setRole(UserRoles.REGULAR);
            Comment comment = new Comment();
            comment.setAuthor(user);
            JwtAuthenticationToken token = createJwtToken("10");
            when(userService.findById(10L)).thenReturn(Optional.of(user));
            when(commentService.findById(5L)).thenReturn(Optional.of(comment));
            ResponseEntity<Void> response = commentController.deleteComment(5L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should succeed for admin")
        void deleteComment_ShouldSucceedForAdmin() {
            User admin = new User();
            admin.setId(1L);
            admin.setRole(UserRoles.ADMIN);
            User author = new User();
            author.setId(2L);
            Comment comment = new Comment();
            comment.setAuthor(author);
            JwtAuthenticationToken token = createJwtToken("1");
            when(userService.findById(1L)).thenReturn(Optional.of(admin));
            when(commentService.findById(5L)).thenReturn(Optional.of(comment));
            ResponseEntity<Void> response = commentController.deleteComment(5L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user not found")
        void deleteComment_UserNotFound() {
            JwtAuthenticationToken token = createJwtToken("10");
            when(userService.findById(10L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = commentController.deleteComment(5L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when comment not found")
        void deleteComment_CommentNotFound() {
            User user = new User();
            user.setId(10L);
            user.setRole(UserRoles.REGULAR);
            JwtAuthenticationToken token = createJwtToken("10");
            when(userService.findById(10L)).thenReturn(Optional.of(user));
            when(commentService.findById(5L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = commentController.deleteComment(5L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return FORBIDDEN for non-author with REGULAR role")
        void deleteComment_ForbiddenForNonAuthor() {
            User user = new User();
            user.setId(10L);
            user.setRole(UserRoles.REGULAR);
            User author = new User();
            author.setId(11L);
            Comment comment = new Comment();
            comment.setAuthor(author);
            JwtAuthenticationToken token = createJwtToken("10");
            when(userService.findById(10L)).thenReturn(Optional.of(user));
            when(commentService.findById(5L)).thenReturn(Optional.of(comment));
            ResponseEntity<Void> response = commentController.deleteComment(5L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("GET /post/{postId}/comment")
    class GetComments {
        @Test
        @DisplayName("Should return comment list for post")
        void getComments_ShouldReturnList() {
            Comment comment = new Comment();
            CommentDto dto = new CommentDto();
            Page<Comment> page = new PageImpl<>(List.of(comment));
            when(commentService.findByPostId(eq(1L), any())).thenReturn(page);
            when(commentMapper.toDto(List.of(comment))).thenReturn(List.of(dto));
            ResponseEntity<List<CommentDto>> response = commentController.getComments(1L, 0, "time");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsExactly(dto);
        }

        @Test
        @DisplayName("Should return empty list when no comments")
        void getComments_ShouldReturnEmptyList() {
            Page<Comment> page = new PageImpl<>(Collections.emptyList());
            when(commentService.findByPostId(eq(1L), any())).thenReturn(page);
            when(commentMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());
            ResponseEntity<List<CommentDto>> response = commentController.getComments(1L, 0, "like");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /post/{postId}/comment/count-page")
    class GetCommentPageCount {
        @Test
        @DisplayName("Should return page count for post comments")
        void getCommentPageCount_ShouldReturnPages() {
            when(commentService.countByPostId(1L)).thenReturn(12L);
            // pageSize默认5，12/5=2.4，ceil=3
            ResponseEntity<Long> response = commentController.getCommentPageCount(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should return 0 when no comments")
        void getCommentPageCount_Zero() {
            when(commentService.countByPostId(1L)).thenReturn(0L);
            ResponseEntity<Long> response = commentController.getCommentPageCount(1L);
            assertThat(response.getBody()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("GET /post/{postId}/comment/count")
    class GetCommentCount {
        @Test
        @DisplayName("Should return comment count for post")
        void getCommentCount_ShouldReturnCount() {
            when(commentService.countByPostId(1L)).thenReturn(7L);
            ResponseEntity<Long> response = commentController.getCommentCount(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("GET /user/{userId}/comment")
    class GetUserComments {
        @Test
        @DisplayName("Should return comment list for user")
        void getUserComments_ShouldReturnList() {
            Comment comment = new Comment();
            CommentDto dto = new CommentDto();
            Page<Comment> page = new PageImpl<>(List.of(comment));
            when(commentService.findByUserId(eq(1L), any())).thenReturn(page);
            when(commentMapper.toDto(List.of(comment))).thenReturn(List.of(dto));
            ResponseEntity<List<CommentDto>> response = commentController.getUserComments(1L, 0);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsExactly(dto);
        }

        @Test
        @DisplayName("Should return empty list when user has no comments")
        void getUserComments_ShouldReturnEmptyList() {
            Page<Comment> page = new PageImpl<>(Collections.emptyList());
            when(commentService.findByUserId(eq(1L), any())).thenReturn(page);
            when(commentMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());
            ResponseEntity<List<CommentDto>> response = commentController.getUserComments(1L, 0);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /user/{userId}/comment/count")
    class GetUserCommentCount {
        @Test
        @DisplayName("Should return comment count for user")
        void getUserCommentCount_ShouldReturnCount() {
            when(commentService.countByUserId(2L)).thenReturn(8L);
            ResponseEntity<Long> response = commentController.getUserCommentCount(2L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(8L);
        }
    }

    @Nested
    @DisplayName("GET /user/{userId}/comment/count-page")
    class GetUserCommentPageCount {
        @Test
        @DisplayName("Should return page count for user comments")
        void getUserCommentPageCount_ShouldReturnPages() {
            when(commentService.countByUserId(2L)).thenReturn(11L);
            ResponseEntity<Long> response = commentController.getUserCommentPageCount(2L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should return 0 when user has no comments")
        void getUserCommentPageCount_Zero() {
            when(commentService.countByUserId(2L)).thenReturn(0L);
            ResponseEntity<Long> response = commentController.getUserCommentPageCount(2L);
            assertThat(response.getBody()).isEqualTo(0L);
        }
    }
}
