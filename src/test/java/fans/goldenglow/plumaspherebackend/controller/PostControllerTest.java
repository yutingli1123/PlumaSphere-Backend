package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.mapper.PostMapper;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.TagService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PostController Tests")
class PostControllerTest {
    private final int PAGE_SIZE = 5;
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Mock
    private TagService tagService;
    @Mock
    private PostMapper postMapper;
    @Mock
    private ConfigService configService;
    @InjectMocks
    private PostController postController;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(configService.get(ConfigField.PAGE_SIZE)).thenReturn(Optional.of(String.valueOf(PAGE_SIZE)));
        postController = new PostController(postService, userService, tagService, postMapper, configService);
        // manually call init
        try {
            var method = PostController.class.getDeclaredMethod("init");
            method.setAccessible(true);
            method.invoke(postController);
        } catch (Exception ignored) {
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Nested
    @DisplayName("GET /api/v1/post")
    class GetPosts {
        @Test
        @DisplayName("Should return paged posts")
        void getPosts_ShouldReturnPagedPosts() {
            List<Post> posts = List.of(new Post());
            Page<Post> page = new PageImpl<>(posts);
            List<PostDto> dtos = List.of(new PostDto());
            when(postService.findAll(any(PageRequest.class))).thenReturn(page);
            when(postMapper.toDto(posts)).thenReturn(dtos);
            ResponseEntity<List<PostDto>> response = postController.getPosts(0);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(dtos);
        }

        @Test
        @DisplayName("Should return empty list when no posts")
        void getPosts_ShouldReturnEmptyList() {
            when(postService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
            when(postMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());
            ResponseEntity<List<PostDto>> response = postController.getPosts(0);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/search")
    class SearchPosts {
        @Test
        @DisplayName("Should return search results")
        void searchPosts_ShouldReturnResults() {
            List<Post> posts = List.of(new Post());
            Page<Post> page = new PageImpl<>(posts);
            List<PostDto> dtos = List.of(new PostDto());
            when(postService.searchPosts(eq("q"), any(PageRequest.class))).thenReturn(page);
            when(postMapper.toDto(posts)).thenReturn(dtos);
            ResponseEntity<List<PostDto>> response = postController.searchPosts("q", 0);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(dtos);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/search/count")
    class GetSearchPostCount {
        @Test
        @DisplayName("Should return search post count")
        void getSearchPostCount_ShouldReturnCount() {
            when(postService.countSearchPosts("q")).thenReturn(10L);
            ResponseEntity<Long> response = postController.getSearchPostCount("q");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/search/count-page")
    class GetSearchPostPageCount {
        @Test
        @DisplayName("Should return search post page count")
        void getSearchPostPageCount_ShouldReturnPageCount() {
            when(postService.countSearchPosts("q")).thenReturn(12L);
            ResponseEntity<Long> response = postController.getSearchPostPageCount("q");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo((long) Math.ceil(12.0 / PAGE_SIZE));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/tag")
    class GetPostsByTag {
        @Test
        @DisplayName("Should return posts by tag")
        void getPostsByTag_ShouldReturnPosts() {
            List<Post> posts = List.of(new Post());
            Page<Post> page = new PageImpl<>(posts);
            List<PostDto> dtos = List.of(new PostDto());
            when(postService.findByTagName(eq("tag"), any(PageRequest.class))).thenReturn(page);
            when(postMapper.toDto(posts)).thenReturn(dtos);
            ResponseEntity<List<PostDto>> response = postController.getPostsByTag("tag", 0);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(dtos);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/tag/count")
    class GetPostCountByTag {
        @Test
        @DisplayName("Should return post count by tag")
        void getPostCountByTag_ShouldReturnCount() {
            when(postService.countByTagName("tag")).thenReturn(7L);
            ResponseEntity<Long> response = postController.getPostCountByTag("tag");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/tag/count-page")
    class GetPostPageCountByTag {
        @Test
        @DisplayName("Should return post page count by tag")
        void getPostPageCountByTag_ShouldReturnPageCount() {
            when(postService.countByTagName("tag")).thenReturn(13L);
            ResponseEntity<Long> response = postController.getPostPageCountByTag("tag");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo((long) Math.ceil(13.0 / PAGE_SIZE));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/count-page")
    class GetPostPageCount {
        @Test
        @DisplayName("Should return post page count")
        void getPostPageCount_ShouldReturnPageCount() {
            when(postService.countPosts()).thenReturn(20L);
            ResponseEntity<Long> response = postController.getPostPageCount();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo((long) Math.ceil(20.0 / PAGE_SIZE));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/count")
    class GetPostCount {
        @Test
        @DisplayName("Should return post count")
        void getPostCount_ShouldReturnCount() {
            when(postService.countPosts()).thenReturn(15L);
            ResponseEntity<Long> response = postController.getPostCount();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(15L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/post/{postId}")
    class GetPost {
        @Test
        @DisplayName("Should return post by id")
        void getPost_ShouldReturnPost() {
            Post post = new Post();
            PostDto dto = new PostDto();
            when(postService.findById(1L)).thenReturn(Optional.of(post));
            when(postMapper.toDto(post)).thenReturn(dto);
            ResponseEntity<PostDto> response = postController.getPost(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(dto);
        }

        @Test
        @DisplayName("Should return NOT_FOUND if post does not exist")
        void getPost_ShouldReturnNotFound() {
            when(postService.findById(1L)).thenReturn(Optional.empty());
            ResponseEntity<PostDto> response = postController.getPost(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/post")
    class CreatePost {
        @Test
        @DisplayName("Should create post when user exists")
        void createPost_ShouldCreate_WhenUserExists() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("1");
            JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
            when(token.getToken()).thenReturn(jwt);
            User user = new User();
            when(userService.findById(1L)).thenReturn(Optional.of(user));
            PostDto dto = new PostDto();
            dto.setTitle("title");
            dto.setContent("content");
            dto.setTags(Collections.emptyList());
            when(tagService.dtoToEntity(anyList())).thenReturn(Collections.emptySet());
            when(postService.generateDescription("content")).thenReturn("desc");
            ResponseEntity<Void> response = postController.createPost(dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(postService).save(any(Post.class));
        }

        @Test
        @DisplayName("Should return NOT_FOUND if user does not exist")
        void createPost_ShouldReturnNotFound_WhenUserNotExist() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("1");
            JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
            when(token.getToken()).thenReturn(jwt);
            when(userService.findById(1L)).thenReturn(Optional.empty());
            PostDto dto = new PostDto();
            ResponseEntity<Void> response = postController.createPost(dto, token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/post (edge cases)")
    class CreatePostEdgeCases {
        @Test
        @DisplayName("Should return UNAUTHORIZED if token is null")
        void createPost_ShouldReturnUnauthorized_WhenTokenNull() {
            PostDto dto = new PostDto();
            ResponseEntity<Void> response = postController.createPost(dto, null);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/post")
    class UpdatePost {
        @Test
        @DisplayName("Should update post when exists")
        void updatePost_ShouldUpdate_WhenExists() {
            Post post = new Post();
            PostDto dto = new PostDto();
            dto.setId(1L);
            dto.setTitle("title");
            dto.setContent("content");
            dto.setTags(Collections.emptyList());
            when(postService.findById(1L)).thenReturn(Optional.of(post));
            when(tagService.dtoToEntity(anyList())).thenReturn(Collections.emptySet());
            when(postService.generateDescription("content")).thenReturn("desc");
            ResponseEntity<Void> response = postController.updatePost(dto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(postService).save(post);
        }

        @Test
        @DisplayName("Should return NOT_FOUND if post does not exist")
        void updatePost_ShouldReturnNotFound_WhenNotExist() {
            PostDto dto = new PostDto();
            dto.setId(1L);
            when(postService.findById(1L)).thenReturn(Optional.empty());
            ResponseEntity<Void> response = postController.updatePost(dto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/post (edge cases)")
    class UpdatePostEdgeCases {
        @Test
        @DisplayName("Should return BAD_REQUEST if dto id is null")
        void updatePost_ShouldReturnBadRequest_WhenIdNull() {
            PostDto dto = new PostDto();
            ResponseEntity<Void> response = postController.updatePost(dto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/post/{postId}")
    class DeletePost {
        @Test
        @DisplayName("Should delete post")
        void deletePost_ShouldDelete() {
            ResponseEntity<Void> response = postController.deletePost(1L);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(postService).delete(1L);
        }
    }
}
