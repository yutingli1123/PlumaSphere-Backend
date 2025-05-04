package fans.goldenglow.plumaspherebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;

    private String title;
    private String content;
    private String description;

    @JsonProperty("author_id")
    private Long authorId;

    private Set<TagDto> tags;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
