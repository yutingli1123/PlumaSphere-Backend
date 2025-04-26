package fans.goldenglow.plumaspherebackend.dto;

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

    private Long authorId;

    private Set<TagDto> tags;

    private Set<Long> likedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
