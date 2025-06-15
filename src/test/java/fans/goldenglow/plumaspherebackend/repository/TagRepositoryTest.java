package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {

    private static final String TEST_TAG_NAME = "testTag";
    private static final String ANOTHER_TAG_NAME = "anotherTag";
    
    private final TagRepository tagRepository;

    @Autowired
    TagRepositoryTest(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Test
    void findByName_ShouldReturnTag_WhenTagExists() {
        // Given
        Tag tag = new Tag(TEST_TAG_NAME);
        tagRepository.save(tag);

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
    void findByName_ShouldReturnEmpty_WhenTagDoesNotExist() {
        // When
        Optional<Tag> foundTag = tagRepository.findByName("nonExistentTag");

        // Then
        assertThat(foundTag).isEmpty();
    }

    @Test
    void findByName_ShouldBeCaseSensitive() {
        // Given
        Tag tag = new Tag(TEST_TAG_NAME);
        tagRepository.save(tag);

        // When
        Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME.toUpperCase());

        // Then
        assertThat(foundTag).isEmpty();
    }

    @Test
    void findByName_ShouldReturnCorrectTag_WhenMultipleTagsExist() {
        // Given
        Tag tag1 = new Tag(TEST_TAG_NAME);
        Tag tag2 = new Tag(ANOTHER_TAG_NAME);
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        // When
        Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME);

        // Then
        assertThat(foundTag)
                .isPresent()
                .get()
                .satisfies(t -> assertThat(t.getName()).isEqualTo(TEST_TAG_NAME));
    }

    @Test
    void save_ShouldPersistTag_WithCorrectProperties() {
        // Given
        Tag tag = new Tag(TEST_TAG_NAME);

        // When
        Tag savedTag = tagRepository.save(tag);

        // Then
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo(TEST_TAG_NAME);

        // Verify it can be found
        Optional<Tag> foundTag = tagRepository.findByName(TEST_TAG_NAME);
        assertThat(foundTag).isPresent();
    }

    @Test
    void findByName_ShouldHandleEmptyName() {
        // When
        Optional<Tag> foundTag = tagRepository.findByName("");

        // Then
        assertThat(foundTag).isEmpty();
    }
}
