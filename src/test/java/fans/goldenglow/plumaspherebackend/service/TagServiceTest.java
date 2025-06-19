package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Tests")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag testTag;

    static Stream<Arguments> edgeCaseTagNames() {
        return Stream.of(
                Arguments.of(List.of(""), 1), // Empty string
                Arguments.of(List.of(" "), 1), // Single space
                Arguments.of(List.of("   "), 1), // Multiple spaces
                Arguments.of(List.of("a"), 1), // Single character
                Arguments.of(List.of("A"), 1), // Single uppercase character
                Arguments.of(List.of("1"), 1), // Number as string
                Arguments.of(List.of("tag-with-dashes"), 1), // Tag with dashes
                Arguments.of(List.of("tag_with_underscores"), 1), // Tag with underscores
                Arguments.of(List.of("tag with spaces"), 1), // Tag with spaces
                Arguments.of(List.of("UPPERCASE"), 1), // Uppercase tag
                Arguments.of(List.of("lowercase"), 1), // Lowercase tag
                Arguments.of(List.of("MiXeDcAsE"), 1) // Mixed case tag
        );
    }

    @BeforeEach
    void setUp() {
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");
    }

    @Nested
    @DisplayName("Find Operations")
    class FindTests {
        @Test
        @DisplayName("Should return all tags when tags exist")
        void findAll_ShouldReturnAllTags_WhenTagsExist() {
            // Given
            List<Tag> expectedTags = List.of(testTag, new Tag("Spring"), new Tag("Boot"));
            when(tagRepository.findAll()).thenReturn(expectedTags);

            // When
            List<Tag> actualTags = tagService.findAll();

            // Then
            assertThat(actualTags).isEqualTo(expectedTags);
            assertThat(actualTags).hasSize(3);
            verify(tagRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no tags exist")
        void findAll_ShouldReturnEmptyList_WhenNoTagsExist() {
            // Given
            when(tagRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<Tag> actualTags = tagService.findAll();

            // Then
            assertThat(actualTags).isEmpty();
            verify(tagRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {
        @Test
        @DisplayName("Should return saved tag when valid tag provided")
        void save_ShouldReturnSavedTag_WhenValidTagProvided() {
            // Given
            Tag tagToSave = new Tag("Python");
            Tag savedTag = new Tag("Python");
            savedTag.setId(2L);
            when(tagRepository.save(tagToSave)).thenReturn(savedTag);

            // When
            Tag actualTag = tagService.save(tagToSave);

            // Then
            assertThat(actualTag).isEqualTo(savedTag);
            assertThat(actualTag.getId()).isEqualTo(2L);
            assertThat(actualTag.getName()).isEqualTo("Python");
            verify(tagRepository).save(tagToSave);
        }

        @Test
        @DisplayName("Should handle tag with existing name")
        void save_ShouldHandleTagWithExistingName() {
            // Given
            Tag existingTag = new Tag("Java");
            existingTag.setId(1L);
            when(tagRepository.save(any(Tag.class))).thenReturn(existingTag);

            // When
            Tag actualTag = tagService.save(existingTag);

            // Then
            assertThat(actualTag).isEqualTo(existingTag);
            verify(tagRepository).save(existingTag);
        }
    }

    @Nested
    @DisplayName("DTO to Entity Conversion")
    class DtoToEntityTests {
        @Test
        @DisplayName("Should return empty set when tags list is null")
        void dtoToEntity_ShouldReturnEmptySet_WhenTagsListIsNull() {
            // When
            Set<Tag> actualTags = tagService.dtoToEntity(null);

            // Then
            assertThat(actualTags).isEmpty();
            verifyNoInteractions(tagRepository);
        }

        @Test
        @DisplayName("Should return empty set when tags list is empty")
        void dtoToEntity_ShouldReturnEmptySet_WhenTagsListIsEmpty() {
            // When
            Set<Tag> actualTags = tagService.dtoToEntity(Collections.emptyList());

            // Then
            assertThat(actualTags).isEmpty();
            verifyNoInteractions(tagRepository);
        }

        @Test
        @DisplayName("Should return existing tags when tags exist in repository")
        void dtoToEntity_ShouldReturnExistingTags_WhenTagsExistInRepository() {
            // Given
            List<String> tagNames = List.of("Java", "Spring");
            Tag javaTag = new Tag("Java");
            javaTag.setId(1L);
            Tag springTag = new Tag("Spring");
            springTag.setId(2L);

            when(tagRepository.findByName("Java")).thenReturn(Optional.of(javaTag));
            when(tagRepository.findByName("Spring")).thenReturn(Optional.of(springTag));

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            assertThat(actualTags).hasSize(2);
            assertThat(actualTags)
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Java", "Spring");
            assertThat(actualTags)
                    .extracting(Tag::getId)
                    .containsExactlyInAnyOrder(1L, 2L);

            verify(tagRepository).findByName("Java");
            verify(tagRepository).findByName("Spring");
        }

        @Test
        @DisplayName("Should create new tags when tags do not exist in repository")
        void dtoToEntity_ShouldCreateNewTags_WhenTagsDoNotExistInRepository() {
            // Given
            List<String> tagNames = List.of("Python", "Django");
            when(tagRepository.findByName("Python")).thenReturn(Optional.empty());
            when(tagRepository.findByName("Django")).thenReturn(Optional.empty());

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            assertThat(actualTags).hasSize(1);
            assertThat(actualTags.iterator().next().getName()).isIn("Python", "Django");
            assertThat(actualTags.iterator().next().getId()).isNull();

            verify(tagRepository).findByName("Python");
            verify(tagRepository).findByName("Django");
        }

        @Test
        @DisplayName("Should mix existing and new tags")
        void dtoToEntity_ShouldMixExistingAndNewTags() {
            // Given
            List<String> tagNames = List.of("Java", "NewFramework");
            Tag existingJavaTag = new Tag("Java");
            existingJavaTag.setId(1L);

            when(tagRepository.findByName("Java")).thenReturn(Optional.of(existingJavaTag));
            when(tagRepository.findByName("NewFramework")).thenReturn(Optional.empty());

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            assertThat(actualTags).hasSize(2);

            Tag javaTag = actualTags.stream()
                    .filter(tag -> "Java".equals(tag.getName()))
                    .findFirst()
                    .orElseThrow();
            Tag newFrameworkTag = actualTags.stream()
                    .filter(tag -> "NewFramework".equals(tag.getName()))
                    .findFirst()
                    .orElseThrow();

            assertThat(javaTag.getId()).isEqualTo(1L); // Existing tag
            assertThat(newFrameworkTag.getId()).isNull(); // New tag

            verify(tagRepository).findByName("Java");
            verify(tagRepository).findByName("NewFramework");
        }

        @Test
        @DisplayName("Should handle duplicate tag names")
        void dtoToEntity_ShouldHandleDuplicateTagNames() {
            // Given
            List<String> tagNames = List.of("Java", "Java", "Spring");
            Tag javaTag = new Tag("Java");
            javaTag.setId(1L);
            Tag springTag = new Tag("Spring");
            springTag.setId(2L);

            when(tagRepository.findByName("Java")).thenReturn(Optional.of(javaTag));
            when(tagRepository.findByName("Spring")).thenReturn(Optional.of(springTag));

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            assertThat(actualTags).hasSize(2); // Set removes duplicates
            assertThat(actualTags)
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Java", "Spring");

            verify(tagRepository, times(2)).findByName("Java"); // Called twice due to duplicate
            verify(tagRepository).findByName("Spring");
        }

        @ParameterizedTest
        @MethodSource("edgeCaseTagNames")
        @DisplayName("Should handle edge case tag names")
        void dtoToEntity_ShouldHandleEdgeCaseTagNames(List<String> tagNames, int expectedSize) {
            // Given
            for (String tagName : tagNames) {
                when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());
            }

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            assertThat(actualTags).hasSize(expectedSize);

            for (String tagName : tagNames) {
                verify(tagRepository).findByName(tagName);
            }
        }

        @Test
        @DisplayName("Should handle large number of tags")
        void dtoToEntity_ShouldHandleLargeNumberOfTags() {
            // Given
            List<String> tagNames = Stream.iterate(0, i -> i + 1)
                    .limit(100)
                    .map(i -> "tag" + i)
                    .toList();

            for (String tagName : tagNames) {
                when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());
            }

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            // Note: Due to Tag entity's @EqualsAndHashCode(of = {"id"}), 
            // new tags with null id are considered equal in Set
            assertThat(actualTags).hasSize(1);
            assertThat(actualTags.iterator().next().getName()).startsWith("tag");
            assertThat(actualTags.iterator().next().getId()).isNull();

            for (String tagName : tagNames) {
                verify(tagRepository).findByName(tagName);
            }
        }

        @Test
        @DisplayName("Should handle single tag")
        void dtoToEntity_ShouldHandleSingleTag() {
            // Given
            List<String> tagNames = List.of("SingleTag");
            when(tagRepository.findByName("SingleTag")).thenReturn(Optional.empty());

            // When
            Set<Tag> actualTags = tagService.dtoToEntity(tagNames);

            // Then
            assertThat(actualTags).hasSize(1);
            assertThat(actualTags.iterator().next().getName()).isEqualTo("SingleTag");
            verify(tagRepository).findByName("SingleTag");
        }
    }
}
