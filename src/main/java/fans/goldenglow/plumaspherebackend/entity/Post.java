package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a post in the PlumaSphere application.
 * This class is used to store information about posts made by users,
 * including the title, content, author, comments, tags, and likes.
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "title", "content"})
@NoArgsConstructor
@Table(name = "pluma_post", indexes = {
        @Index(name = "idx_post_author_id", columnList = "author_id")
})
public class Post implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    @Basic(fetch = FetchType.LAZY)
    private String content;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "post", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "pluma_post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pluma_post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"})
    )
    private Set<User> likedBy = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
