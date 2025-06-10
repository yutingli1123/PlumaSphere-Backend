package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryTest {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private User savedUser;
    private Post savedPost;

    @Autowired
    public CommentRepositoryTest(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @BeforeEach
    public void Setup() {
        User user = new User("username", "password");
        savedUser = userRepository.save(user);

        Post post = new Post();
        post.setTitle("title");
        post.setContent("content");
        post.setDescription("description");
        post.setAuthor(savedUser);
        savedPost = postRepository.save(post);
    }

    @Test
    public void testFindByPostId() {
        Long postId = savedPost.getId();

        Comment comment = new Comment();
        comment.setContent("testContent");
        comment.setPost(savedPost);
        comment.setAuthor(savedUser);
        commentRepository.save(comment);

        Page<Comment> comments = commentRepository.findByPostId(postId, PageRequest.of(0, 10));
        assertThat(comments)
                .hasSize(1)
                .first()
                .satisfies(c -> assertThat(c.getContent()).isEqualTo("testContent"));
    }

    @Test
    public void testCountByPostId() {
        Long postId = savedPost.getId();

        Comment comment = new Comment();
        comment.setContent("testContent");
        comment.setPost(savedPost);
        comment.setAuthor(savedUser);
        commentRepository.save(comment);

        Long count = commentRepository.countByPostId(postId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testFindByParentCommentId() {
        Comment parentComment = new Comment();
        parentComment.setContent("parentContent");
        parentComment.setAuthor(savedUser);
        Comment savedParentComment = commentRepository.save(parentComment);

        Long parentCommentId = savedParentComment.getId();

        Comment childComment = new Comment();
        childComment.setContent("childContent");
        childComment.setAuthor(savedUser);
        childComment.setParentComment(savedParentComment);
        commentRepository.save(childComment);

        Page<Comment> comments = commentRepository.findByParentCommentId(parentCommentId, PageRequest.of(0, 10));
        assertThat(comments)
                .hasSize(1)
                .first()
                .satisfies(c -> assertThat(c.getContent()).isEqualTo("childContent"));
    }

    @Test
    public void testFindByAuthorId() {
        Long authorId = savedUser.getId();

        Comment comment = new Comment();
        comment.setContent("testContent");
        comment.setAuthor(savedUser);
        commentRepository.save(comment);

        Page<Comment> comments = commentRepository.findByAuthorId(authorId, PageRequest.of(0, 10));
        assertThat(comments)
                .hasSize(1)
                .first()
                .satisfies(c -> assertThat(c.getContent()).isEqualTo("testContent"));
    }

    @Test
    public void testCountByAuthorId() {
        Long authorId = savedUser.getId();

        Comment comment = new Comment();
        comment.setContent("testContent");
        comment.setAuthor(savedUser);
        commentRepository.save(comment);

        Long count = commentRepository.countByAuthorId(authorId);
        assertThat(count).isEqualTo(1);
    }
}
