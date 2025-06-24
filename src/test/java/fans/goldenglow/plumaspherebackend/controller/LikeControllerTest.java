package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.websocket.WebSocketMessageDto;
import fans.goldenglow.plumaspherebackend.handler.WebSocketHandler;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.LikeCacheService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("LikeController Tests")
class LikeControllerTest {
    @Mock
    private LikeCacheService likeCacheService;
    @Mock
    private WebSocketHandler webSocketHandler;
    @Mock
    private CommentService commentService;
    @InjectMocks
    private LikeController likeController;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Nested
    @DisplayName("GET /post/{postId}/like")
    class GetPostLikes {
        @Test
        @DisplayName("Should return post like count")
        void getLikes_ShouldReturnCount() {
            when(likeCacheService.getPostLikesCount(1L)).thenReturn(5L);
            ResponseEntity<Long> response = likeController.getLikes(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("GET /comment/{commentId}/like")
    class GetCommentLikes {
        @Test
        @DisplayName("Should return comment like count")
        void getCommentLikes_ShouldReturnCount() {
            when(likeCacheService.getCommentLikesCount(2L)).thenReturn(3L);
            ResponseEntity<Long> response = likeController.getCommentLikes(2L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("GET /post/{postId}/like/state")
    class GetPostLikeState {
        @Test
        @DisplayName("Should return like state for post and user")
        void getPostLikeState_ShouldReturnState() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("10");
            JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
            when(token.getToken()).thenReturn(jwt);

            when(likeCacheService.isPostLiked(1L, 10L)).thenReturn(true);
            ResponseEntity<Boolean> response = likeController.getPostLikeState(1L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /comment/{commentId}/like/state")
    class GetCommentLikeState {
        @Test
        @DisplayName("Should return like state for comment and user")
        void getCommentLikeState_ShouldReturnState() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("10");
            JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
            when(token.getToken()).thenReturn(jwt);

            when(likeCacheService.isCommentLiked(2L, 10L)).thenReturn(false);
            ResponseEntity<Boolean> response = likeController.getCommentLikeState(2L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
        }
    }

    @Nested
    @DisplayName("POST /post/{postId}/like")
    class LikePost {
        @Test
        @DisplayName("Should like post and send websocket message")
        void likePost_ShouldSucceed() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("10");
            JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
            when(token.getToken()).thenReturn(jwt);

            ResponseEntity<Void> response = likeController.likePost(1L, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(likeCacheService).switchPostLike(1L, 10L);
            verify(webSocketHandler).sendMessageToPost(eq(1L), any(WebSocketMessageDto.class));
        }
    }
}
