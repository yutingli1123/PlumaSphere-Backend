package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for posts.
 * This class is used to represent a post with its ID, title, content, description, author ID, tags,
 * and timestamps for creation and last update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;

    private String title;
    private String content;
    private String description;
    private Long authorId;
    private List<String> tags;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
