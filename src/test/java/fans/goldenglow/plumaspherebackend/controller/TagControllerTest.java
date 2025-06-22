package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.service.TagService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("TagController Tests")
class TagControllerTest {
    @Mock
    private TagService tagService;
    @InjectMocks
    private TagController tagController;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Nested
    @DisplayName("GET /api/v1/tag")
    class GetAllTags {
        @Test
        @DisplayName("Should return all tags sorted by post count")
        void getAllTags_ShouldReturnList() {
            Tag tag = new Tag("tag1");
            tag.setId(1L);
            tag.setPosts(Collections.emptySet());
            TagDto dto = new TagDto(1L, "tag1", 0);
            when(tagService.findAll()).thenReturn(List.of(tag));
            ResponseEntity<List<TagDto>> response = tagController.getAllTags();
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).containsExactly(dto);
        }

        @Test
        @DisplayName("Should return empty list when no tags")
        void getAllTags_ShouldReturnEmpty() {
            when(tagService.findAll()).thenReturn(Collections.emptyList());
            ResponseEntity<List<TagDto>> response = tagController.getAllTags();
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/tag")
    class AddTag {
        @Test
        @DisplayName("Should add tag successfully")
        void addTag_ShouldSucceed() {
            TagDto dto = new TagDto(null, "tag2", 0);
            ResponseEntity<Void> response = tagController.addTag(dto);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }
    }
}
