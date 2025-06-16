package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    private static final String NON_EXISTENT_TAG = "nonExistentTag";
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private User savedUser;
    private Tag savedTag;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @BeforeEach
    void setupPost() {
        User user = new User(TEST_USERNAME, "testPassword");
        savedUser = userRepository.save(user);

        Tag tag = new Tag(TAG_NAME);
        savedTag = tagRepository.save(tag);

        Post post = new Post();
        post.setTitle(TEST_TITLE);
        post.setContent(TEST_CONTENT);
        post.setDescription(TEST_DESCRIPTION);
        post.setAuthor(savedUser);
        post.setTags(Set.of(savedTag));
        postRepository.save(post);
    }

    @Test
    void findByTagsName_ShouldReturnPost_WhenTagExists() {
        // When
        Page<Post> foundPosts = postRepository.findByTagsName(TAG_NAME, PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts)
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
    void findByTagsName_ShouldReturnEmpty_WhenTagDoesNotExist() {
        // When
        Page<Post> foundPosts = postRepository.findByTagsName(NON_EXISTENT_TAG, PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts).isEmpty();
    }

    @Test
    void findByTagsName_ShouldHandleMultiplePosts_WithSameTag() {
        // Given
        Post anotherPost = new Post();
        anotherPost.setTitle("anotherTitle");
        anotherPost.setContent("anotherContent");
        anotherPost.setDescription("anotherDescription");
        anotherPost.setAuthor(savedUser);
        anotherPost.setTags(Set.of(savedTag));
        postRepository.save(anotherPost);

        // When
        Page<Post> foundPosts = postRepository.findByTagsName(TAG_NAME, PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts).hasSize(2);
    }

    @Test
    void countByTagsName_ShouldReturnCorrectCount_WhenTagExists() {
        // When
        Long count = postRepository.countByTagsName(TAG_NAME);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByTagsName_ShouldReturnZero_WhenTagDoesNotExist() {
        // When
        Long count = postRepository.countByTagsName(NON_EXISTENT_TAG);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource({
            "title, 1",
            "TITLE, 1",      // Case-insensitive
            "content, 1",
            "CONTENT, 1",  // Case-insensitive
            "description, 1",
            "DESCRIPTION, 1", // Case-insensitive
            "nonExistent, 0",
            "randomText, 0"
    })
    void searchByKeyword_ShouldReturnCorrectResults(String keyword, int expectedCount) {
        // When - Search in title, content, and description
        Page<Post> foundPosts = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                keyword, keyword, keyword, PageRequest.of(0, 10));

        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                keyword, keyword, keyword);

        // Then
        assertThat(foundPosts).hasSize(expectedCount);
        assertThat(count).isEqualTo(expectedCount);

        if (expectedCount > 0) {
            assertThat(foundPosts.getContent().getFirst())
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
    }

    @Test
    void searchBySpecificField_ShouldWork_WhenSearchingOnlyTitle() {
        // When - Search only in title field
        Page<Post> foundPosts = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                "title", null, null, PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts).hasSize(1);
    }

    @Test
    void searchBySpecificField_ShouldWork_WhenSearchingOnlyContent() {
        // When - Search only in content field
        Page<Post> foundPosts = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                null, "content", null, PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts).hasSize(1);
    }

    @Test
    void searchBySpecificField_ShouldWork_WhenSearchingOnlyDescription() {
        // When - Search only in description field
        Page<Post> foundPosts = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                null, null, "description", PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts).hasSize(1);
    }

    @Test
    void searchByMismatchedFields_ShouldReturnEmpty() {
        // When - Search with mismatched field values
        Page<Post> foundPosts1 = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                null, TEST_TITLE, TEST_TITLE, PageRequest.of(0, 10));

        Page<Post> foundPosts2 = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                TEST_CONTENT, null, TEST_CONTENT, PageRequest.of(0, 10));

        Page<Post> foundPosts3 = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                TEST_DESCRIPTION, TEST_DESCRIPTION, null, PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts1).isEmpty();
        assertThat(foundPosts2).isEmpty();
        assertThat(foundPosts3).isEmpty();
    }

    @Test
    void searchByEmptyKeyword_ShouldReturnAllPosts() {
        // When
        Page<Post> foundPosts = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                "", "", "", PageRequest.of(0, 10));

        // Then - Empty string matches all posts
        assertThat(foundPosts).isNotEmpty();
        assertThat(foundPosts.getContent().size()).isGreaterThan(0);
    }

    @Test
    void findByTagsName_ShouldBeCaseSensitive() {
        // When
        Page<Post> foundPosts = postRepository.findByTagsName(TAG_NAME.toUpperCase(), PageRequest.of(0, 10));

        // Then
        assertThat(foundPosts).isEmpty();
    }

    @Test
    void searchMethods_ShouldHandlePagination() {
        // Given - Create multiple posts to test pagination
        for (int i = 1; i <= 5; i++) {
            Post post = new Post();
            post.setTitle("testTitle" + i);
            post.setContent("testContent" + i);
            post.setDescription("testDescription" + i);
            post.setAuthor(savedUser);
            post.setTags(Set.of(savedTag));
            postRepository.save(post);
        }

        // When - Request page with size 3
        Page<Post> page = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                "test", "test", "test", PageRequest.of(0, 3));

        // Then
        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(6); // 5 new + 1 from setup
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void countMethods_ShouldBeConsistent_WithFindMethods() {
        // Given
        String keyword = "test";

        // When
        Page<Post> foundPosts = postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                keyword, keyword, keyword, PageRequest.of(0, 10));
        Long count = postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                keyword, keyword, keyword);

        // Then
        assertThat(foundPosts.getTotalElements()).isEqualTo(count);
    }
}
