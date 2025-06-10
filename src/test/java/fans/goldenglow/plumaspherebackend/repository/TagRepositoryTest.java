package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {
    private final TagRepository tagRepository;

    @Autowired
    TagRepositoryTest(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Test
    public void testFindByName() {
        Tag tag = new Tag("testTag");
        tagRepository.save(tag);
        Optional<Tag> foundTag = tagRepository.findByName("testTag");
        assertThat(foundTag)
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("testTag");
    }
}
