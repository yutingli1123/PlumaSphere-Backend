package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/post")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDto>> getPosts() {
        List<Post> posts = postService.findAll();
        List<PostDto> postDtos = new ArrayList<>();
        posts.forEach(post -> postDtos.add(new PostDto(post.getId(), post.getTitle(), post.getContent(),
                post.getAuthor().getUsername(), post.getTags(), post.getCategories(), post.getLikes(),
                post.getCreatedAt(), post.getUpdatedAt())));
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        Optional<Post> post = postService.findById(postId);
        if (post.isPresent()) {
            Post postEntity = post.get();
            PostDto postDto = new PostDto(postEntity.getId(), postEntity.getTitle(), postEntity.getContent(),
                    postEntity.getAuthor().getUsername(), postEntity.getTags(), postEntity.getCategories(),
                    postEntity.getLikes(), postEntity.getCreatedAt(), postEntity.getUpdatedAt());
            return ResponseEntity.ok(postDto);
        }
        return ResponseEntity.notFound().build();
    }


}
