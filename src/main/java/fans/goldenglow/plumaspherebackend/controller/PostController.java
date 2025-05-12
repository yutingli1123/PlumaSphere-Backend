package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.TagService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final TagService tagService;
    private final int pageSize;

    @Autowired
    public PostController(PostService postService, UserService userService, TagService tagService, ConfigService configService) {
        this.postService = postService;
        this.userService = userService;
        this.tagService = tagService;
        Optional<String> pageSizeConfig = configService.get(ConfigField.PAGE_SIZE);
        pageSize = pageSizeConfig.map(Integer::parseInt).orElse(5);
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam int page) {
        Page<Post> postsPage = postService.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(postsPage.getContent().stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getTitle(),
                        null,
                        post.getDescription(),
                        post.getAuthor().getId(),
                        post.getTags().stream().map(tag -> new TagDto(tag.getId(), tag.getName())).collect(Collectors.toSet()),
                        post.getCreatedAt().atZone(ZoneId.systemDefault()),
                        post.getUpdatedAt().atZone(ZoneId.systemDefault())))
                .collect(Collectors.toList()));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPostsCount() {
        long count = postService.countPosts();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);

        if (post.isEmpty()) return ResponseEntity.notFound().build();

        Post postEntity = post.get();
        PostDto postDto = new PostDto(
                postEntity.getId(),
                postEntity.getTitle(),
                postEntity.getContent(),
                postEntity.getDescription(),
                postEntity.getAuthor().getId(),
                postEntity.getTags().stream().map(tag -> new TagDto(tag.getId(), tag.getName())).collect(Collectors.toSet()),
                postEntity.getCreatedAt().atZone(ZoneId.systemDefault()),
                postEntity.getUpdatedAt().atZone(ZoneId.systemDefault()));
        return ResponseEntity.ok(postDto);
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody PostDto postDto, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        User userEntity = user.get();
        Long postId = postDto.getId();
        Post postEntity;
        if (postId != null) {
            Optional<Post> post = postService.findById(postId);
            if (post.isPresent()) {
                String content = postDto.getContent();
                postEntity = post.get();
                postEntity.setTitle(postDto.getTitle());
                postEntity.setContent(content);
                postEntity.setTags(tagService.dtoToEntity(postDto.getTags()));
                postEntity.setDescription(content.substring(0, Math.min(content.length(), 300)) + "...");
                postService.save(postEntity);
                return ResponseEntity.ok().build();
            }
        }
        String content = postDto.getContent();
        postEntity = new Post();
        postEntity.setTitle(postDto.getTitle());
        postEntity.setContent(content);
        postEntity.setAuthor(userEntity);
        postEntity.setTags(tagService.dtoToEntity(postDto.getTags()));
        postEntity.setDescription(content.substring(0, Math.min(content.length(), 300)) + "...");
        postService.save(postEntity);
        return ResponseEntity.ok().build();
    }
}
