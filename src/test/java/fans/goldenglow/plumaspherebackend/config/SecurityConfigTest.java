package fans.goldenglow.plumaspherebackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fans.goldenglow.plumaspherebackend.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisTestConfiguration.class)
public class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /**
     * Set up JWT authentication for the current thread for testing purposes.
     * Scopes should be in the format "SCOPE_admin", "SCOPE_regular", etc.
     */
    private void setJwtAuthentication(String userId, String... scopes) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(userId)
                .claim("scope", String.join(" ", Arrays.stream(scopes).map(s -> s.replace("SCOPE_", "")).collect(Collectors.toList())))
                .build();
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwt,
                Arrays.stream(scopes).map(SimpleGrantedAuthority::new).collect(Collectors.toList())
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Nested
    @DisplayName("/api/v1/init/** endpoints - Custom access control based on initialization status")
    class InitEndpoints {

        @Test
        @DisplayName("Should be accessible when system is not initialized")
        void testInitEndpointsWhenNotInitialized() throws Exception {
            mockMvc.perform(post("/api/v1/init")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/init/verify-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/init/status"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Should be forbidden when system is initialized")
        void testInitEndpointsWhenInitialized() throws Exception {
            mockMvc.perform(post("/api/v1/init")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/init/verify-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/init/status"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }
    }

    @Nested
    @DisplayName("PermitAll endpoints - No authentication required")
    class PermitAllEndpoints {

        @Test
        @DisplayName("Login and authentication endpoints should be accessible without authentication")
        void testLoginEndpoints() throws Exception {
            // /api/v1/login
            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /api/v1/refresh-token
            mockMvc.perform(post("/api/v1/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /api/v1/get-identity
            mockMvc.perform(get("/api/v1/get-identity"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Status endpoints should be accessible without authentication")
        void testStatusEndpoints() throws Exception {
            // /api/v1/status/**
            mockMvc.perform(get("/api/v1/status"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/status/version"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/status/health"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Public and error endpoints should be accessible without authentication")
        void testPublicAndErrorEndpoints() throws Exception {
            // /public/**
            mockMvc.perform(get("/public/index.html"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/public/assets/style.css"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /error/**
            mockMvc.perform(get("/error/404"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/error/500"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("WebSocket endpoints should be accessible without authentication")
        void testWebSocketEndpoints() throws Exception {
            // /ws/**
            mockMvc.perform(get("/ws/connect"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/ws/info"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }
    }

    @Nested
    @DisplayName("Admin scope required for specific GET endpoints")
    class AdminScopeGetEndpoints {

        @Test
        @DisplayName("Should be accessible with admin scope")
        void testAdminScopeGetEndpointsWithAdmin() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin");
            // /api/v1/user/count
            mockMvc.perform(get("/api/v1/user/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /api/v1/user/count-page
            mockMvc.perform(get("/api/v1/user/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Should be forbidden without admin scope")
        void testAdminScopeGetEndpointsWithoutAdmin() throws Exception {
            setJwtAuthentication("1", "SCOPE_regular");
            mockMvc.perform(get("/api/v1/user/count"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/user/count-page"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should be unauthorized without authentication")
        void testAdminScopeGetEndpointsWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/user/count"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/user/count-page"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Public GET endpoints - No authentication required for GET requests")
    class PublicGetEndpoints {

        @Test
        @DisplayName("Post endpoints should be accessible without authentication for GET")
        void testPostGetEndpoints() throws Exception {
            // /api/v1/post/**
            mockMvc.perform(get("/api/v1/post"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/search"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/search/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/search/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/tag"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/tag/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/tag/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Comment endpoints should be accessible without authentication for GET")
        void testCommentGetEndpoints() throws Exception {
            // /api/v1/comment/**
            mockMvc.perform(get("/api/v1/comment/1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/1/comment"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/1/comment/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/1/comment/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/user/1/comment"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/user/1/comment/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/user/1/comment/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/comment/1/reply"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Tag endpoints should be accessible without authentication for GET")
        void testTagGetEndpoints() throws Exception {
            // /api/v1/tag
            mockMvc.perform(get("/api/v1/tag"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("User profile endpoints should be accessible without authentication for GET")
        void testUserProfileGetEndpoints() throws Exception {
            // /api/v1/user/{userId}
            mockMvc.perform(get("/api/v1/user/1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/user/999"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Upload endpoints should be accessible without authentication for GET")
        void testUploadGetEndpoints() throws Exception {
            // /upload/**
            mockMvc.perform(get("/upload/image.jpg"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/upload/files/document.pdf"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/upload/avatars/user1.png"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Like endpoints should be accessible without authentication for GET")
        void testLikeGetEndpoints() throws Exception {
            // Like endpoints (GET methods are public)
            mockMvc.perform(get("/api/v1/post/1/like"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/comment/1/like"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/post/1/like/state"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/comment/1/like/state"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }
    }

    @Nested
    @DisplayName("Non-GET methods on public endpoints require admin scope")
    class NonGetMethodsOnPublicEndpoints {

        @Test
        @DisplayName("Should be accessible with admin scope")
        void testNonGetMethodsWithAdmin() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin");
            PostDto postDto = new PostDto();
            postDto.setTitle("Test Title");
            postDto.setContent("Test Content");

            TagDto tagDto = new TagDto();
            tagDto.setName("Test Tag");

            UserDto userDto = new UserDto();
            userDto.setNickname("testuser");

            // POST to /api/v1/post
            mockMvc.perform(post("/api/v1/post")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // PUT to /api/v1/post
            mockMvc.perform(put("/api/v1/post/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // DELETE to /api/v1/post
            mockMvc.perform(delete("/api/v1/post/1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // POST to /api/v1/tag
            mockMvc.perform(post("/api/v1/tag")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tagDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // PUT to /api/v1/user (general user management)
            mockMvc.perform(put("/api/v1/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // DELETE to /api/v1/user
            mockMvc.perform(delete("/api/v1/user/1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Should be forbidden without admin scope")
        void testNonGetMethodsWithoutAdmin() throws Exception {
            setJwtAuthentication("1", "SCOPE_regular");
            mockMvc.perform(post("/api/v1/post")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/v1/post")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/v1/post/1"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/tag")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should be unauthorized without authentication")
        void testNonGetMethodsWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/v1/post")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(delete("/api/v1/post/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authenticated endpoints - Require authentication but no specific scope")
    class AuthenticatedEndpoints {

        @Test
        @DisplayName("Should be accessible with any valid authentication")
        void testAuthenticatedEndpointsWithAuth() throws Exception {
            setJwtAuthentication("1", "SCOPE_regular");
            CommentDto commentDto = new CommentDto();
            commentDto.setContent("Test comment");

            // /api/v1/post/{postId}/comment
            mockMvc.perform(post("/api/v1/post/1/comment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /api/v1/post/{postId}/like
            mockMvc.perform(post("/api/v1/post/1/like"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /api/v1/comment/** (non-GET methods)
            mockMvc.perform(post("/api/v1/comment/1/reply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // /api/v1/user/me
            mockMvc.perform(get("/api/v1/user/me"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // Like endpoints (POST methods require authentication)
            mockMvc.perform(post("/api/v1/comment/1/like"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Should work with admin authentication as well")
        void testAuthenticatedEndpointsWithAdminAuth() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin");
            CommentDto commentDto = new CommentDto();
            commentDto.setContent("Test comment by admin");

            mockMvc.perform(post("/api/v1/post/1/comment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/user/me"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Should be unauthorized without authentication")
        void testAuthenticatedEndpointsWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/v1/post/1/comment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/post/1/like"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(delete("/api/v1/comment/1"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/user/me"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/comment/1/like"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Admin only endpoints - All other requests require admin scope")
    class AdminOnlyEndpoints {

        @Test
        @DisplayName("Should be accessible with admin scope")
        void testAdminOnlyEndpointsWithAdmin() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin");
            BanRequestDto banRequestDto = new BanRequestDto();
            banRequestDto.setUserId(2L); // Ban a different user
            banRequestDto.setReason("Test ban");

            BanIPRequestDto banIPRequestDto = new BanIPRequestDto();
            banIPRequestDto.setIpAddress("127.0.0.1");

            StringDto stringDto = new StringDto("http://example.com/file.txt");

            // Admin controller endpoints
            mockMvc.perform(post("/api/v1/admin/ban-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(banRequestDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(delete("/api/v1/admin/unban-user")
                            .param("userId", "2"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/banned-users"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/banned-users/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/banned-users/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/marked-users"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/marked-users/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/marked-users/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/user-ban-status")
                            .param("userId", "2"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/admin/mark-user-for-ip-ban")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(banRequestDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(delete("/api/v1/admin/unmark-user-ip-ban")
                            .param("userId", "2"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/admin/ban-ip")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(banIPRequestDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(delete("/api/v1/admin/unban-ip")
                            .param("ip", "192.168.1.1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/banned-ips"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/banned-ips/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/api/v1/admin/banned-ips/count-page"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // File controller endpoints (admin required)
            mockMvc.perform(post("/api/v1/file/upload")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/file/fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stringDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(multipart("/api/v1/user/avatar").file("file", "test-avatar.jpg".getBytes()))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // System settings (admin required)
            mockMvc.perform(post("/api/v1/settings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // Any other endpoint not explicitly mentioned
            mockMvc.perform(get("/api/v1/some/random/endpoint"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/another/endpoint")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Should be forbidden without admin scope")
        void testAdminOnlyEndpointsWithoutAdmin() throws Exception {
            setJwtAuthentication("1", "SCOPE_regular");
            mockMvc.perform(post("/api/v1/admin/ban-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/admin/banned-users"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/file/upload")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/settings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/v1/some/random/endpoint"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should be unauthorized without authentication")
        void testAdminOnlyEndpointsWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/v1/admin/ban-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/admin/banned-users"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/file/upload"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/some/random/endpoint"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Edge cases and boundary conditions")
    class EdgeCases {

        @Test
        @DisplayName("Path variations and special cases")
        void testPathVariations() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin");
            // Test paths with trailing slashes
            mockMvc.perform(get("/api/v1/post/1/"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // Test paths with query parameters
            mockMvc.perform(get("/api/v1/post/search?q=test&page=1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // Test nested upload paths
            mockMvc.perform(get("/upload/2023/12/image.jpg"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(get("/upload/user/1/avatar.png"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // Test deeply nested public paths
            mockMvc.perform(get("/public/assets/images/logo.png"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // Test WebSocket subpaths
            mockMvc.perform(get("/ws/chat/room/1"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Different HTTP methods on same path")
        void testDifferentHttpMethods() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin");
            String testPath = "/api/v1/post/1";
            PostDto postDto = new PostDto();
            postDto.setTitle("Test Title");
            postDto.setContent("Test Content");

            // GET should be public
            mockMvc.perform(get(testPath))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            // POST/PUT/DELETE should require admin (unauthorized without auth)
            mockMvc.perform(post(testPath + "/comment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CommentDto())))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(put(testPath)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postDto)))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(delete(testPath))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("Mixed scope authorities")
        void testMixedScopeAuthorities() throws Exception {
            setJwtAuthentication("1", "SCOPE_admin", "SCOPE_regular");
            // Should work with multiple scopes including admin
            mockMvc.perform(get("/api/v1/user/count"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

            mockMvc.perform(post("/api/v1/admin/ban-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
        }
    }
}
