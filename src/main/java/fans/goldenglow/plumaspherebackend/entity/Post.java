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
    private Set<Comment> comments = new HashSet<>();
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();
    @ManyToMany
    private Set<Category> categories;
    @OneToMany
    private Set<User> likedBy = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post(String title, String content, User author, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
