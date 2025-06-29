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
 * Controller for handling post-related operations.
 * Provides endpoints to create, update, delete, and retrieve posts,
 * as well as search and filter posts by tags.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final TagService tagService;
    private final PostMapper postMapper;
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
     * Retrieves a paginated list of posts.
     *
     * @param page the page number to retrieve
     * @return a ResponseEntity containing a list of PostDto objects
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam int page) {
        Page<Post> postsPage = postService.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postMapper.toDto(postsPage.getContent()));
    }

    /**
     * Retrieves the total number of pages for all posts.
     *
     * @return a ResponseEntity containing the total number of pages for all posts
     */
    @GetMapping("/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostPageCount() {
        long totalPosts = postService.countPosts();
        long totalPages = (long) Math.ceil((double) totalPosts / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    /**
     * Retrieves the total count of all posts.
     *
     * @return a ResponseEntity containing the total count of all posts
     */
    @GetMapping("/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostCount() {
        return ResponseEntity.ok(postService.countPosts());
    }

    /**
     * Searches for posts based on a query string.
     *
     * @param query the search query string
     * @param page  the page number to retrieve
     * @return a ResponseEntity containing a list of PostDto objects that match the search query
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDto>> searchPosts(@RequestParam String query, @RequestParam int page) {
        Page<Post> postsPage = postService.searchPosts(query, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postMapper.toDto(postsPage.getContent()));
    }

    /**
     * Retrieves the total count of posts that match a search query.
     *
     * @param query the search query string
     * @return a ResponseEntity containing the total count of matching posts
     */
    @GetMapping("/search/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getSearchPostCount(@RequestParam String query) {
        long totalPosts = postService.countSearchPosts(query);
        return ResponseEntity.ok(totalPosts);
    }

    /**
     * Retrieves the total number of pages for posts that match a search query.
     *
     * @param query the search query string
     * @return a ResponseEntity containing the total number of pages for matching posts
     */
    @GetMapping("/search/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getSearchPostPageCount(@RequestParam String query) {
        long totalPosts = postService.countSearchPosts(query);
        long totalPages = (long) Math.ceil((double) totalPosts / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    /**
     * Retrieves a paginated list of posts by tag name.
     *
     * @param tagName the name of the tag to filter posts
     * @param page the page number to retrieve
     * @return a ResponseEntity containing a list of PostDto objects that match the tag
     */
    @GetMapping("/tag")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDto>> getPostsByTag(@RequestParam String tagName, @RequestParam int page) {
        Page<Post> postsPage = postService.findByTagName(tagName, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postMapper.toDto(postsPage.getContent()));
    }

    /**
     * Retrieves the total count of posts by tag name.
     *
     * @param tagName the name of the tag to filter posts
     * @return a ResponseEntity containing the total count of posts with the specified tag
     */
    @GetMapping("/tag/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostCountByTag(@RequestParam String tagName) {
        long totalPosts = postService.countByTagName(tagName);
        return ResponseEntity.ok(totalPosts);
    }

    /**
     * Retrieves the total number of pages for posts by tag name.
     *
     * @param tagName the name of the tag to filter posts
     * @return a ResponseEntity containing the total number of pages for posts with the specified tag
     */
    @GetMapping("/tag/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostPageCountByTag(@RequestParam String tagName) {
        long totalPosts = postService.countByTagName(tagName);
        long totalPages = (long) Math.ceil((double) totalPosts / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param postId the ID of the post to retrieve
     * @return a ResponseEntity containing the PostDto object if found, or a 404 Not Found status
     */
    @GetMapping("/{postId}")
    @Transactional(readOnly = true)
    public ResponseEntity<PostDto> getPost(@PathVariable("postId") Long postId) {
        Optional<Post> post = postService.findById(postId);
        return post.map(value -> ResponseEntity.ok(postMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new post.
     * Requires authentication via JWT token.
     *
     * @param postDto the DTO containing post data
     * @param token   the JWT authentication token of the user
     * @return a ResponseEntity indicating the result of the operation
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Void> createPost(@RequestBody PostDto postDto, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        User userEntity = user.get();
        String content = postDto.getContent();
        Post postEntity = new Post();
        postEntity.setTitle(postDto.getTitle());
        postEntity.setContent(content);
        postEntity.setAuthor(userEntity);
        postEntity.setTags(tagService.dtoToEntity(postDto.getTags()));
        postEntity.setDescription(postService.generateDescription(content));
        postService.save(postEntity);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates an existing post.
     * Requires authentication via JWT token.
     *
     * @param postDto the DTO containing updated post data
     * @return a ResponseEntity indicating the result of the operation
     */
    @PutMapping
    @Transactional
    public ResponseEntity<Void> updatePost(@RequestBody PostDto postDto) {
        Long postId = postDto.getId();
        if (postId == null) return ResponseEntity.badRequest().build();
        Optional<Post> post = postService.findById(postId);
        if (post.isPresent()) {
            String content = postDto.getContent();
            Post postEntity = post.get();
            postEntity.setTitle(postDto.getTitle());
            postEntity.setContent(content);
            postEntity.setTags(tagService.dtoToEntity(postDto.getTags()));
            postEntity.setDescription(postService.generateDescription(content));
            postService.save(postEntity);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a post by its ID.
     * Requires authentication via JWT token.
     *
     * @param postId the ID of the post to delete
     * @return a ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        postService.delete(postId);
        return ResponseEntity.ok().build();
    }
}
