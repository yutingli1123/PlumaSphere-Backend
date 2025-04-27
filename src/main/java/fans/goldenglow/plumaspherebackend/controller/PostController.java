package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.TagService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final TagService tagService;

    @Autowired
    public PostController(PostService postService, UserService userService, TagService tagService) {
        this.postService = postService;
        this.userService = userService;
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<Set<PostDto>> getPosts() {
        List<Post> posts = postService.findAll();
        return ResponseEntity.ok(posts.stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getAuthor().getId(),
                        post.getTags().stream()
                                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                                .collect(Collectors.toSet()),
                        post.getLikedBy().stream()
                                .map(User::getId)
                                .collect(Collectors.toSet()),
                        post.getCreatedAt(),
                        post.getUpdatedAt()))
                .collect(Collectors.toSet()));
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
                postEntity.getAuthor().getId(),
                postEntity.getTags().stream().map(tag -> new TagDto(tag.getId(), tag.getName())).collect(Collectors.toSet()),
                postEntity.getLikedBy().stream().map(User::getId).collect(Collectors.toSet()),
                postEntity.getCreatedAt(),
                postEntity.getUpdatedAt());
        return ResponseEntity.ok(postDto);
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody PostDto postDto, JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        User userEntity = user.get();
        Long postId = postDto.getId();
        if (postId != null) {
            Optional<Post> post = postService.findById(postId);
            if (post.isPresent()) {
                Post postEntity = post.get();
                postEntity.setTitle(postDto.getTitle());
                postEntity.setContent(postDto.getContent());
                postEntity.setTags(tagService.dtoToEntity(postDto.getTags()));
                postService.save(postEntity);
                return ResponseEntity.ok().build();
            }
        }
        Post newPost = new Post();
        newPost.setTitle(postDto.getTitle());
        newPost.setContent(postDto.getContent());
        newPost.setAuthor(userEntity);
        newPost.setTags(tagService.dtoToEntity(postDto.getTags()));
        postService.save(newPost);
        return ResponseEntity.ok().build();
    }
}
