package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MarkdownService markdownService;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Title");
        testPost.setContent("Test Content");
        testPost.setDescription("Test Description");

        testPageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Count Operations")
    class CountTests {
        @Test
        @DisplayName("Should return count when repository returns count")
        void countPosts_ShouldReturnCount_WhenRepositoryReturnsCount() {
            // Given
            long expectedCount = 5L;
            when(postRepository.count()).thenReturn(expectedCount);

            // When
            long actualCount = postService.countPosts();

            // Then
            assertThat(actualCount).isEqualTo(expectedCount);
            verify(postRepository).count();
        }

        @Test
        @DisplayName("Should return zero when repository returns zero")
        void countPosts_ShouldReturnZero_WhenRepositoryReturnsZero() {
            // Given
            when(postRepository.count()).thenReturn(0L);

            // When
            long actualCount = postService.countPosts();

            // Then
            assertThat(actualCount).isZero();
            verify(postRepository).count();
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindTests {
        @Test
        @DisplayName("Should return page of posts when posts exist")
        void findAll_ShouldReturnPageOfPosts_WhenPostsExist() {
            // Given
            List<Post> posts = List.of(testPost);
            Page<Post> expectedPage = new PageImpl<>(posts, testPageable, 1);
            when(postRepository.findAll(testPageable)).thenReturn(expectedPage);

            // When
            Page<Post> actualPage = postService.findAll(testPageable);

            // Then
            assertThat(actualPage).isEqualTo(expectedPage);
            assertThat(actualPage.getContent()).hasSize(1);
            assertThat(actualPage.getContent().getFirst()).isEqualTo(testPost);
            verify(postRepository).findAll(testPageable);
        }

        @Test
        @DisplayName("Should return empty page when no posts exist")
        void findAll_ShouldReturnEmptyPage_WhenNoPostsExist() {
            // Given
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
            when(postRepository.findAll(testPageable)).thenReturn(emptyPage);

            // When
            Page<Post> actualPage = postService.findAll(testPageable);

            // Then
            assertThat(actualPage).isEqualTo(emptyPage);
            assertThat(actualPage.getContent()).isEmpty();
            verify(postRepository).findAll(testPageable);
        }

        @Test
        @DisplayName("Should return post when post exists by id")
        void findById_ShouldReturnPost_WhenPostExists() {
            // Given
            Long postId = 1L;
            when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));

            // When
            Optional<Post> actualPost = postService.findById(postId);

            // Then
            assertThat(actualPost).isPresent();
            assertThat(actualPost.get()).isEqualTo(testPost);
            verify(postRepository).findById(postId);
        }

        @Test
        @DisplayName("Should return empty when post does not exist by id")
        void findById_ShouldReturnEmpty_WhenPostDoesNotExist() {
            // Given
            Long postId = 999L;
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            // When
            Optional<Post> actualPost = postService.findById(postId);

            // Then
            assertThat(actualPost).isEmpty();
            verify(postRepository).findById(postId);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
        @DisplayName("Should handle edge case ids for findById")
        void findById_ShouldHandleEdgeCaseIds(Long postId) {
            // Given
            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            // When
            Optional<Post> actualPost = postService.findById(postId);

            // Then
            assertThat(actualPost).isEmpty();
            verify(postRepository).findById(postId);
        }
    }

    @Nested
    @DisplayName("Tag Operations")
    class TagTests {
        @Test
        @DisplayName("Should return page of posts when tag exists")
        void findByTagName_ShouldReturnPageOfPosts_WhenTagExists() {
            // Given
            String tagName = "java";
            List<Post> posts = List.of(testPost);
            Page<Post> expectedPage = new PageImpl<>(posts, testPageable, 1);
            when(postRepository.findByTagsName(tagName, testPageable)).thenReturn(expectedPage);

            // When
            Page<Post> actualPage = postService.findByTagName(tagName, testPageable);

            // Then
            assertThat(actualPage).isEqualTo(expectedPage);
            assertThat(actualPage.getContent()).hasSize(1);
            verify(postRepository).findByTagsName(tagName, testPageable);
        }

        @Test
        @DisplayName("Should return empty page when tag does not exist")
        void findByTagName_ShouldReturnEmptyPage_WhenTagDoesNotExist() {
            // Given
            String tagName = "nonexistent";
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
            when(postRepository.findByTagsName(tagName, testPageable)).thenReturn(emptyPage);

            // When
            Page<Post> actualPage = postService.findByTagName(tagName, testPageable);

            // Then
            assertThat(actualPage).isEmpty();
            verify(postRepository).findByTagsName(tagName, testPageable);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "JAVA", "java", "Java"})
        @DisplayName("Should handle various tag names")
        void findByTagName_ShouldHandleVariousTagNames(String tagName) {
            // Given
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
            when(postRepository.findByTagsName(tagName, testPageable)).thenReturn(emptyPage);

            // When
            Page<Post> actualPage = postService.findByTagName(tagName, testPageable);

            // Then
            assertThat(actualPage).isEmpty();
            verify(postRepository).findByTagsName(tagName, testPageable);
        }

        @Test
        @DisplayName("Should return count when tag exists")
        void countByTagName_ShouldReturnCount_WhenTagExists() {
            // Given
            String tagName = "spring";
            long expectedCount = 3L;
            when(postRepository.countByTagsName(tagName)).thenReturn(expectedCount);

            // When
            long actualCount = postService.countByTagName(tagName);

            // Then
            assertThat(actualCount).isEqualTo(expectedCount);
            verify(postRepository).countByTagsName(tagName);
        }

        @Test
        @DisplayName("Should return zero when tag does not exist")
        void countByTagName_ShouldReturnZero_WhenTagDoesNotExist() {
            // Given
            String tagName = "nonexistent";
            when(postRepository.countByTagsName(tagName)).thenReturn(0L);

            // When
            long actualCount = postService.countByTagName(tagName);

            // Then
            assertThat(actualCount).isZero();
            verify(postRepository).countByTagsName(tagName);
        }
    }

    @Nested
    @DisplayName("Save and Delete Operations")
    class SaveDeleteTests {
        @Test
        @DisplayName("Should call repository save with given post")
        void save_ShouldCallRepositorySave_WithGivenPost() {
            // When
            postService.save(testPost);

            // Then
            verify(postRepository).save(testPost);
        }

        @Test
        @DisplayName("Should call repository deleteById with given id")
        void delete_ShouldCallRepositoryDeleteById_WithGivenId() {
            // Given
            Long postId = 1L;

            // When
            postService.delete(postId);

            // Then
            verify(postRepository).deleteById(postId);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
        @DisplayName("Should handle edge case ids for delete")
        void delete_ShouldHandleEdgeCaseIds(Long postId) {
            // When
            postService.delete(postId);

            // Then
            verify(postRepository).deleteById(postId);
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchTests {
        @Test
        @DisplayName("Should return page of posts when keyword matches")
        void searchPosts_ShouldReturnPageOfPosts_WhenKeywordMatches() {
            // Given
            String keyword = "test";
            List<Post> posts = List.of(testPost);
            Page<Post> expectedPage = new PageImpl<>(posts, testPageable, 1);
            when(postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword, testPageable)).thenReturn(expectedPage);

            // When
            Page<Post> actualPage = postService.searchPosts(keyword, testPageable);

            // Then
            assertThat(actualPage).isEqualTo(expectedPage);
            assertThat(actualPage.getContent()).hasSize(1);
            verify(postRepository).findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword, testPageable);
        }

        @Test
        @DisplayName("Should return empty page when keyword does not match")
        void searchPosts_ShouldReturnEmptyPage_WhenKeywordDoesNotMatch() {
            // Given
            String keyword = "nomatch";
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
            when(postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword, testPageable)).thenReturn(emptyPage);

            // When
            Page<Post> actualPage = postService.searchPosts(keyword, testPageable);

            // Then
            assertThat(actualPage).isEmpty();
            verify(postRepository).findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword, testPageable);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "TEST", "test", "Test"})
        @DisplayName("Should handle various keywords")
        void searchPosts_ShouldHandleVariousKeywords(String keyword) {
            // Given
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
            when(postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword, testPageable)).thenReturn(emptyPage);

            // When
            Page<Post> actualPage = postService.searchPosts(keyword, testPageable);

            // Then
            assertThat(actualPage).isEmpty();
            verify(postRepository).findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword, testPageable);
        }

        @Test
        @DisplayName("Should return count when keyword matches")
        void countSearchPosts_ShouldReturnCount_WhenKeywordMatches() {
            // Given
            String keyword = "spring";
            long expectedCount = 2L;
            when(postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword)).thenReturn(expectedCount);

            // When
            Long actualCount = postService.countSearchPosts(keyword);

            // Then
            assertThat(actualCount).isEqualTo(expectedCount);
            verify(postRepository).countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword);
        }

        @Test
        @DisplayName("Should return zero when keyword does not match")
        void countSearchPosts_ShouldReturnZero_WhenKeywordDoesNotMatch() {
            // Given
            String keyword = "nomatch";
            when(postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword)).thenReturn(0L);

            // When
            Long actualCount = postService.countSearchPosts(keyword);

            // Then
            assertThat(actualCount).isZero();
            verify(postRepository).countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    keyword, keyword, keyword);
        }
    }

    @Nested
    @DisplayName("Description Generation")
    class DescriptionTests {
        @Test
        @DisplayName("Should return truncated description when content is long")
        void generateDescription_ShouldReturnTruncatedDescription_WhenContentIsLong() {
            // Given
            String longContent = "a".repeat(400);
            String plainText = "b".repeat(400);
            when(markdownService.convertMarkdownToPlainText(longContent)).thenReturn(plainText);

            // When
            String description = postService.generateDescription(longContent);

            // Then
            assertThat(description).hasSize(303); // 300 + "..."
            assertThat(description).endsWith("...");
            assertThat(description.substring(0, 300)).isEqualTo("b".repeat(300));
            verify(markdownService).convertMarkdownToPlainText(longContent);
        }

        @Test
        @DisplayName("Should return full description when content is short")
        void generateDescription_ShouldReturnFullDescription_WhenContentIsShort() {
            // Given
            String shortContent = "Short content";
            String plainText = "Short plain text";
            when(markdownService.convertMarkdownToPlainText(shortContent)).thenReturn(plainText);

            // When
            String description = postService.generateDescription(shortContent);

            // Then
            assertThat(description).isEqualTo(plainText);
            assertThat(description).doesNotEndWith("...");
            verify(markdownService).convertMarkdownToPlainText(shortContent);
        }

        @ParameterizedTest
        @CsvSource({
                "'', ''",
                "'Short', 'Short'",
                "'Exactly 300 chars', 'Exactly 300 chars'"
        })
        @DisplayName("Should handle edge cases for description generation")
        void generateDescription_ShouldHandleEdgeCases(String content, String plainText) {
            // Given
            when(markdownService.convertMarkdownToPlainText(content)).thenReturn(plainText);

            // When
            String description = postService.generateDescription(content);

            // Then
            assertThat(description).isEqualTo(plainText);
            verify(markdownService).convertMarkdownToPlainText(content);
        }

        @Test
        @DisplayName("Should handle exactly 300 characters for description")
        void generateDescription_ShouldHandleExactly300Characters() {
            // Given
            String content = "test";
            String plainText = "a".repeat(300);
            when(markdownService.convertMarkdownToPlainText(content)).thenReturn(plainText);

            // When
            String description = postService.generateDescription(content);

            // Then
            assertThat(description).isEqualTo(plainText);
            assertThat(description).hasSize(300);
            assertThat(description).doesNotEndWith("...");
            verify(markdownService).convertMarkdownToPlainText(content);
        }
    }
}
