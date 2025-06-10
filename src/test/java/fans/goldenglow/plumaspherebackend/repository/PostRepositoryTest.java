package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {
    private static final String TEST_USERNAME = "testUser";
    private static final String TAG_NAME = "testTag";
    private static final String TEST_TITLE = "testTitle";
    private static final String TEST_CONTENT = "testContent";
    private static final String TEST_DESCRIPTION = "testDescription";
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;


    @Autowired
    public PostRepositoryTest(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @BeforeEach
    public void setupPost() {
        User user = new User(TEST_USERNAME, "testPassword");
        User savedUser = userRepository.save(user);

        Tag tag = new Tag(TAG_NAME);
        Tag savedTag = tagRepository.save(tag);

        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(TEST_CONTENT);
        post.setDescription(TEST_DESCRIPTION);
        post.setAuthor(savedUser);
        post.setTags(Set.of(savedTag));
        postRepository.save(post);
    }

    @Test
    public void testFindByTagsName() {
        Page<Post> foundPost = postRepository.findByTagsName(TAG_NAME, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(1)
                .first()
                .satisfies(post -> {
                    assertThat(post.getTitle()).isEqualTo(TEST_TITLE);
                    assertThat(post.getContent()).isEqualTo(TEST_CONTENT);
                    assertThat(post.getDescription()).isEqualTo(TEST_DESCRIPTION);
                    assertThat(post.getAuthor().getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(post.getTags())
                            .extracting(Tag::getName)
                            .containsExactly(TAG_NAME);
                });
    }

    @Test
    public void testCountByTagsName() {
        Long count = postRepository.countByTagsName(TAG_NAME);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testSearch1() {
        String keyword = "title";
        Page<Post> foundPost = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, null, null, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(1)
                .first()
                .satisfies(post -> {
                    assertThat(post.getTitle()).isEqualTo(TEST_TITLE);
                    assertThat(post.getContent()).isEqualTo(TEST_CONTENT);
                    assertThat(post.getDescription()).isEqualTo(TEST_DESCRIPTION);
                    assertThat(post.getAuthor().getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(post.getTags())
                            .extracting(Tag::getName)
                            .containsExactly(TAG_NAME);
                });

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testSearch2() {
        String keyword = "content";
        Page<Post> foundPost = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(null, keyword, null, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(1)
                .first()
                .satisfies(post -> {
                    assertThat(post.getTitle()).isEqualTo(TEST_TITLE);
                    assertThat(post.getContent()).isEqualTo(TEST_CONTENT);
                    assertThat(post.getDescription()).isEqualTo(TEST_DESCRIPTION);
                    assertThat(post.getAuthor().getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(post.getTags())
                            .extracting(Tag::getName)
                            .containsExactly(TAG_NAME);
                });

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testSearch3() {
        String keyword = "description";
        Page<Post> foundPost = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(null, null, keyword, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(1)
                .first()
                .satisfies(post -> {
                    assertThat(post.getTitle()).isEqualTo(TEST_TITLE);
                    assertThat(post.getContent()).isEqualTo(TEST_CONTENT);
                    assertThat(post.getDescription()).isEqualTo(TEST_DESCRIPTION);
                    assertThat(post.getAuthor().getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(post.getTags())
                            .extracting(Tag::getName)
                            .containsExactly(TAG_NAME);
                });

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testSearch4() {
        Page<Post> foundPost = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(null, TEST_TITLE, TEST_TITLE, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(0);

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(null, TEST_TITLE, TEST_TITLE);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testSearch5() {
        Page<Post> foundPost = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(TEST_CONTENT, null, TEST_CONTENT, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(0);

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(TEST_CONTENT, null, TEST_CONTENT);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testSearch6() {
        Page<Post> foundPost = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(TEST_DESCRIPTION, TEST_DESCRIPTION, null, PageRequest.of(0, 10));
        assertThat(foundPost)
                .hasSize(0);

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(TEST_DESCRIPTION, TEST_DESCRIPTION, null);
        assertThat(count).isEqualTo(0);
    }

}
