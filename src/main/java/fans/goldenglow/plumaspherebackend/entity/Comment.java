package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_comment")
public class Comment {
    @Id
    @GeneratedValue
    private Long id;
    private String content;
    @OneToOne
    private User author;
    @ManyToOne
    private Post post;
    @OneToMany
    private List<Like> likes;
    private LocalDateTime createdAt;
}
