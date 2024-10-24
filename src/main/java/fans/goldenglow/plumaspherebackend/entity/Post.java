package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_post")
public class Post {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String content;
    @ManyToOne
    private User author;
    @OneToMany
    private List<Comment> comments = new ArrayList<>();
    @ManyToMany
    private List<Tag> tags = new ArrayList<>();
    @ManyToOne
    private Category categories;
    @OneToMany
    private List<Like> likes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
