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

    @PostConstruct
    private void init() {
        Optional<String> pageSizeConfig = configService.get(ConfigField.PAGE_SIZE);
        pageSize = pageSizeConfig.map(Integer::parseInt).orElse(5);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam int page) {
        Page<Post> postsPage = postService.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postMapper.toDto(postsPage.getContent()));
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDto>> searchPosts(@RequestParam String query, @RequestParam int page) {
        Page<Post> postsPage = postService.searchPosts(query, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postMapper.toDto(postsPage.getContent()));
    }

    @GetMapping("/search/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getSearchPostCount(@RequestParam String query) {
        long totalPosts = postService.countSearchPosts(query);
        return ResponseEntity.ok(totalPosts);
    }

    @GetMapping("/search/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getSearchPostPageCount(@RequestParam String query) {
        long totalPosts = postService.countSearchPosts(query);
        long totalPages = (long) Math.ceil((double) totalPosts / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    @GetMapping("/tag")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDto>> getPostsByTag(@RequestParam String tagName, @RequestParam int page) {
        Page<Post> postsPage = postService.findByTagName(tagName, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postMapper.toDto(postsPage.getContent()));
    }

    @GetMapping("/tag/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostCountByTag(@RequestParam String tagName) {
        long totalPosts = postService.countByTagName(tagName);
        return ResponseEntity.ok(totalPosts);
    }

    @GetMapping("/tag/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostPageCountByTag(@RequestParam String tagName) {
        long totalPosts = postService.countByTagName(tagName);
        long totalPages = (long) Math.ceil((double) totalPosts / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    @GetMapping("/count-page")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostPageCount() {
        long totalPosts = postService.countPosts();
        long totalPages = (long) Math.ceil((double) totalPosts / (double) pageSize);
        return ResponseEntity.ok(totalPages);
    }

    @GetMapping("/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> getPostCount() {
        return ResponseEntity.ok(postService.countPosts());
    }

    @GetMapping("/{postId}")
    @Transactional(readOnly = true)
    public ResponseEntity<PostDto> getPost(@PathVariable("postId") Long postId) {
        Optional<Post> post = postService.findById(postId);
        return post.map(value -> ResponseEntity.ok(postMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

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

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        postService.delete(postId);
        return ResponseEntity.ok().build();
    }
}
