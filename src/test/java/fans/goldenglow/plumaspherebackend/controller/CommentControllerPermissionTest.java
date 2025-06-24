package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.config.EmbeddedRedisTestConfiguration;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.CommentService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisTestConfiguration.class)
@DisplayName("CommentController Permission Tests")
public class CommentControllerPermissionTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private void setJwtAuthentication(String... scopes) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("1")
                .claim("scope", Arrays.stream(scopes).map(s -> s.replace("SCOPE_", "")).collect(Collectors.joining(" ")))
                .build();
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwt,
                Arrays.stream(scopes).map(SimpleGrantedAuthority::new).collect(Collectors.toList())
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    @DisplayName("User should be able to delete their own comment")
    void testUserCanDeleteOwnComment() throws Exception {
        User author = new User();
        author.setId(1L);
        author.setRole(UserRoles.REGULAR);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setAuthor(author);

        when(userService.findById(1L)).thenReturn(Optional.of(author));
        when(commentService.findById(100L)).thenReturn(Optional.of(comment));

        setJwtAuthentication("SCOPE_regular");

        mockMvc.perform(delete("/api/v1/comment/100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User should be forbidden from deleting another user's comment")
    void testUserCannotDeleteOthersComment() throws Exception {
        User attacker = new User();
        attacker.setId(1L);
        attacker.setRole(UserRoles.REGULAR);

        User originalAuthor = new User();
        originalAuthor.setId(2L);
        originalAuthor.setRole(UserRoles.REGULAR);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setAuthor(originalAuthor);

        when(userService.findById(1L)).thenReturn(Optional.of(attacker));
        when(commentService.findById(100L)).thenReturn(Optional.of(comment));

        setJwtAuthentication("SCOPE_regular");

        mockMvc.perform(delete("/api/v1/comment/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should be able to delete any comment")
    void testAdminCanDeleteAnyComment() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRoles.ADMIN);

        User originalAuthor = new User();
        originalAuthor.setId(2L);
        originalAuthor.setRole(UserRoles.REGULAR);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setAuthor(originalAuthor);

        when(userService.findById(1L)).thenReturn(Optional.of(admin));
        when(commentService.findById(100L)).thenReturn(Optional.of(comment));

        setJwtAuthentication("SCOPE_admin");

        mockMvc.perform(delete("/api/v1/comment/100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated user should be unauthorized to delete any comment")
    void testUnauthenticatedUserCannotDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/v1/comment/100"))
                .andExpect(status().isUnauthorized());
    }
} 