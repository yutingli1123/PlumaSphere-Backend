package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a tag.
 * This class is used to represent a tag with its ID, name, and post count.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {
    private Long id;
    private String name;
    private Integer postCount;

    public TagDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
