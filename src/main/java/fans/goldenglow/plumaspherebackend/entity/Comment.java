package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_comment")
public class Comment {
    @Id
    @GeneratedValue
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    @ManyToOne
    private User author;
    @ManyToOne
    private Post post;
    @OneToMany
    private Set<User> likedBy = new HashSet<>();
    @OneToMany
    private Set<Comment> comments = new HashSet<>();
}
