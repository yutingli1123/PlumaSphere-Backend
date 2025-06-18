package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TagRepository Tests")
class TagRepositoryTest {

    private static final String TEST_TAG_NAME = "testTag";
    private static final String ANOTHER_TAG_NAME = "anotherTag";
    
    private final TagRepository tagRepository;
    private final TestEntityManager entityManager;

    @Autowired
    TagRepositoryTest(TagRepository tagRepository, TestEntityManager entityManager) {
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
    }

    @Nested
    @DisplayName("Find by Name Operations")
    class FindByNameTests {

        @Test
        @DisplayName("Should find tag by name when tag exists")
        void findByName_ShouldReturnTag_WhenTagExists() {
            // Given
            Tag tag = new Tag(TEST_TAG_NAME);
            entityManager.persistAndFlush(tag);

            // When
            Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME);

            // Then
            assertThat(foundTag)
                    .isPresent()
                    .get()
                    .satisfies(t -> {
                        assertThat(t.getName()).isEqualTo(TEST_TAG_NAME);
                        assertThat(t.getId()).isNotNull();
                    });
        }

        @Test
        @DisplayName("Should return empty when tag does not exist")
        void findByName_ShouldReturnEmpty_WhenTagDoesNotExist() {
            // When
            Optional<Tag> foundTag = tagRepository.findByName("nonExistentTag");

            // Then
            assertThat(foundTag).isEmpty();
        }

        @Test
        @DisplayName("Should be case sensitive")
        void findByName_ShouldBeCaseSensitive() {
            // Given
            Tag tag = new Tag(TEST_TAG_NAME);
            entityManager.persistAndFlush(tag);

            // When
            Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME.toUpperCase());

            // Then
            assertThat(foundTag).isEmpty();
        }

        @Test
        @DisplayName("Should return correct tag when multiple tags exist")
        void findByName_ShouldReturnCorrectTag_WhenMultipleTagsExist() {
            // Given
            Tag tag1 = new Tag(TEST_TAG_NAME);
            Tag tag2 = new Tag(ANOTHER_TAG_NAME);
            entityManager.persistAndFlush(tag1);
            entityManager.persistAndFlush(tag2);

            // When
            Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME);

            // Then
            assertThat(foundTag)
                    .isPresent()
                    .get()
                    .satisfies(t -> assertThat(t.getName()).isEqualTo(TEST_TAG_NAME));
        }

        @ParameterizedTest
        @DisplayName("Should return empty for invalid names")
        @ValueSource(strings = {"", " ", "  "})
        void findByName_ShouldReturnEmpty_ForInvalidNames(String invalidName) {
            // When
            Optional<Tag> foundTag = tagRepository.findByName(invalidName);

            // Then
            assertThat(foundTag).isEmpty();
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should persist tag with correct properties")
        void save_ShouldPersistTag_WithCorrectProperties() {
            // Given
            Tag tag = new Tag(TEST_TAG_NAME);

            // When
            Tag savedTag = entityManager.persistAndFlush(tag);

            // Then
            assertThat(savedTag.getId()).isNotNull();
            assertThat(savedTag.getName()).isEqualTo(TEST_TAG_NAME);

            // Verify it can be found
            Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME);
            assertThat(foundTag).isPresent();
        }
    }
}
