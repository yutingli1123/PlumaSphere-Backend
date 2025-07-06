package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PostRepository Tests")
class PostRepositoryTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TAG_NAME = "testTag";
    private static final String TEST_TITLE = "testTitle";
    private static final String TEST_CONTENT = "testContent";
    private static final String TEST_DESCRIPTION = "testDescription";
    private static final String NON_EXISTENT_TAG = "nonExistentTag";
    
    private final PostRepository postRepository;
    private final TestEntityManager entityManager;
    private User savedUser;
    private Tag savedTag;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, TestEntityManager entityManager) {
        this.postRepository = postRepository;
        this.entityManager = entityManager;
    }

    private User createAndPersistUser() {
        User user = new User(PostRepositoryTest.TEST_USERNAME, TEST_PASSWORD);
        return entityManager.persistAndFlush(user);
    }

    private Tag createAndPersistTag() {
        Tag tag = new Tag(PostRepositoryTest.TAG_NAME);
        return entityManager.persistAndFlush(tag);
    }

    private void createAndPersistPost(User author, Tag... tags) {
        Post post = new Post();
        post.setTitle(PostRepositoryTest.TEST_TITLE);
        post.setContent(PostRepositoryTest.TEST_CONTENT);
        post.setDescription(TEST_DESCRIPTION);
        post.setAuthor(author);
        if (tags.length > 0) {
            post.setTags(Set.of(tags));
        }
        entityManager.persistAndFlush(post);
    }

    @BeforeEach
    void setUp() {
        savedUser = createAndPersistUser();
        savedTag = createAndPersistTag();
        createAndPersistPost(savedUser, savedTag);
    }

    @Nested
    @DisplayName("Tag Operations")
    class TagOperationsTests {
        @Test
        @DisplayName("Should return post when tag exists")
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
        @DisplayName("Should return empty when tag does not exist")
        void findByTagsName_ShouldReturnEmpty_WhenTagDoesNotExist() {
            // When
            Page<Post> foundPosts = postRepository.findByTagsName(NON_EXISTENT_TAG, PageRequest.of(0, 10));

            // Then
            assertThat(foundPosts).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple posts with same tag")
        void findByTagsName_ShouldHandleMultiplePosts_WithSameTag() {
            // Given
            Post anotherPost = new Post();
            anotherPost.setTitle("anotherTitle");
            anotherPost.setContent("anotherContent");
            anotherPost.setDescription("anotherDescription");
            anotherPost.setAuthor(savedUser);
            anotherPost.setTags(Set.of(savedTag));
            entityManager.persistAndFlush(anotherPost);

            // When
            Page<Post> foundPosts = postRepository.findByTagsName(TAG_NAME, PageRequest.of(0, 10));

            // Then
            assertThat(foundPosts).hasSize(2);
        }

        @Test
        @DisplayName("Should return correct count when tag exists")
        void countByTagsName_ShouldReturnCorrectCount_WhenTagExists() {
            // When
            Long count = postRepository.countByTagsName(TAG_NAME);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return zero when tag does not exist")
        void countByTagsName_ShouldReturnZero_WhenTagDoesNotExist() {
            // When
            Long count = postRepository.countByTagsName(NON_EXISTENT_TAG);

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should be case sensitive for tag name")
        void findByTagsName_ShouldBeCaseSensitive() {
            // When
            Page<Post> foundPosts = postRepository.findByTagsName(TAG_NAME.toUpperCase(), PageRequest.of(0, 10));

            // Then
            assertThat(foundPosts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Keyword Search Operations")
    class KeywordSearchOperationsTests {
        @ParameterizedTest
        @CsvSource({
                "title, 1",
                "TITLE, 1",
                "content, 1",
                "CONTENT, 1",
                "description, 1",
                "DESCRIPTION, 1",
                "nonExistent, 0",
                "randomText, 0"
        })
        @DisplayName("Should return correct results for keyword search")
        void searchByKeyword_ShouldReturnCorrectResults(String keyword, int expectedCount) {
            // When - Search in title, content, and description
            Page<Post> foundPosts = postRepository.searchByKeyword(keyword, PageRequest.of(0, 10));

            Long count = postRepository.countByKeyword(keyword);

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
        @DisplayName("Should search across all fields (title, content, description)")
        void searchByKeyword_ShouldSearchAcrossAllFields() {
            // Given - Create posts with different keywords in different fields
            Post titlePost = new Post();
            titlePost.setTitle("uniqueTitle");
            titlePost.setContent("different content");
            titlePost.setDescription("different description");
            titlePost.setAuthor(savedUser);
            entityManager.persistAndFlush(titlePost);

            Post contentPost = new Post();
            contentPost.setTitle("different title");
            contentPost.setContent("uniqueContent");
            contentPost.setDescription("different description");
            contentPost.setAuthor(savedUser);
            entityManager.persistAndFlush(contentPost);

            Post descriptionPost = new Post();
            descriptionPost.setTitle("different title");
            descriptionPost.setContent("different content");
            descriptionPost.setDescription("uniqueDescription");
            descriptionPost.setAuthor(savedUser);
            entityManager.persistAndFlush(descriptionPost);

            // When - Search for keywords that exist in each field
            Page<Post> titleResults = postRepository.searchByKeyword("uniqueTitle", PageRequest.of(0, 10));
            Page<Post> contentResults = postRepository.searchByKeyword("uniqueContent", PageRequest.of(0, 10));
            Page<Post> descriptionResults = postRepository.searchByKeyword("uniqueDescription", PageRequest.of(0, 10));

            // Then - Each search should find the corresponding post
            assertThat(titleResults).hasSize(1);
            assertThat(contentResults).hasSize(1);
            assertThat(descriptionResults).hasSize(1);
        }

        @Test
        @DisplayName("Should return all posts for empty keyword")
        void searchByEmptyKeyword_ShouldReturnAllPosts() {
            // When
            Page<Post> foundPosts = postRepository.searchByKeyword("", PageRequest.of(0, 10));

            // Then - Empty string matches all posts
            assertThat(foundPosts).isNotEmpty();
            assertThat(foundPosts.getContent().size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle pagination for search methods")
        void searchMethods_ShouldHandlePagination() {
            // Given - Create multiple posts to test pagination
            for (int i = 1; i <= 5; i++) {
                Post post = new Post();
                post.setTitle("testTitle" + i);
                post.setContent("testContent" + i);
                post.setDescription("testDescription" + i);
                post.setAuthor(savedUser);
                post.setTags(Set.of(savedTag));
                entityManager.persistAndFlush(post);
            }

            // When - Request page with size 3
            Page<Post> page = postRepository.searchByKeyword("test", PageRequest.of(0, 3));

            // Then
            assertThat(page.getSize()).isEqualTo(3);
            assertThat(page.getTotalElements()).isEqualTo(6); // 5 new + 1 from setup
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("Count methods should be consistent with find methods")
        void countMethods_ShouldBeConsistent_WithFindMethods() {
            // Given
            String keyword = "test";

            // When
            Page<Post> foundPosts = postRepository.searchByKeyword(keyword, PageRequest.of(0, 10));
            Long count = postRepository.countByKeyword(keyword);

            // Then
            assertThat(foundPosts.getTotalElements()).isEqualTo(count);
        }
    }
}
