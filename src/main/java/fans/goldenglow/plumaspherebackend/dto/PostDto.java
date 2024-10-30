package fans.goldenglow.plumaspherebackend.dto;

import fans.goldenglow.plumaspherebackend.entity.Category;
import fans.goldenglow.plumaspherebackend.entity.Like;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;

    private String title;
    private String content;

    private String author;

    private List<Tag> tags = new ArrayList<>();

    private Category categories;

    private List<Like> likes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
