package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.CategoryDto;
import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.CategoryService;
import fans.goldenglow.plumaspherebackend.service.PostService;
import fans.goldenglow.plumaspherebackend.service.TagService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/post")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<Set<PostDto>> getPosts() {
        List<Post> posts = postService.findAll();
        return ResponseEntity.ok(posts.stream()
                .map(post -> new PostDto(
                        post.getId(), post.getTitle(), post.getContent(), post.getAuthor().getUsername(),
                        post.getTags().stream()
                                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                                .collect(Collectors.toSet()),
                        post.getCategories().stream()
                                .map(category -> new CategoryDto(category.getId(), category.getName()))
                                .collect(Collectors.toSet()),
                        post.getLikedBy().stream()
                                .map(User::getUsername)
                                .collect(Collectors.toSet()),
                        post.getCreatedAt(), post.getUpdatedAt()))
                .collect(Collectors.toSet()));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);
        if (post.isPresent()) {
            Post postEntity = post.get();
            PostDto postDto = new PostDto(postEntity.getId(), postEntity.getTitle(), postEntity.getContent(),
                    postEntity.getAuthor().getUsername(),
                    postEntity.getTags().stream().map(tag -> new TagDto(tag.getId(), tag.getName())).collect(Collectors.toSet()),
                    postEntity.getCategories().stream().map(category -> new CategoryDto(category.getId(), category.getName())).collect(Collectors.toSet()),
                    postEntity.getLikedBy().stream().map(User::getUsername).collect(Collectors.toSet()),
                    postEntity.getCreatedAt(), postEntity.getUpdatedAt());
            return ResponseEntity.ok(postDto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Boolean> createPost(@RequestBody PostDto postDto) {
        Optional<User> user = userService.findByUsername(postDto.getAuthor());
        if (user.isPresent()) {
            User userEntity = user.get();
            Set<Post> posts = userEntity.getPosts();
            Long postId = postDto.getId();
            if (postId != null) {
                Optional<Post> post = postService.findById(postId);
                if (post.isPresent()) {
                    Post postEntity = post.get();
                    postEntity.setTitle(postDto.getTitle());
                    postEntity.setContent(postDto.getContent());
                    postEntity.setCategories(categoryService.dtoToEntity(postDto.getCategories()));
                    postEntity.setTags(tagService.dtoToEntity(postDto.getTags()));
                    postEntity.setUpdatedAt(LocalDateTime.now());

                    posts.add(postEntity);
                    userEntity.setPosts(posts);
                    return ResponseEntity.ok(postService.save(postEntity) && userService.save(userEntity));
                }
            }
            Post newPost = new Post();
            newPost.setTitle(postDto.getTitle());
            newPost.setContent(postDto.getContent());
            newPost.setAuthor(userEntity);
            newPost.setCategories(categoryService.dtoToEntity(postDto.getCategories()));
            newPost.setTags(tagService.dtoToEntity(postDto.getTags()));
            newPost.setUpdatedAt(LocalDateTime.now());
            newPost.setCreatedAt(LocalDateTime.now());
            posts.add(newPost);
            userEntity.setPosts(posts);
            return ResponseEntity.ok(postService.save(newPost) && userService.save(userEntity));
        }
        return ResponseEntity.notFound().build();
    }
}
