package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a comment in the PlumaSphere application.
 * This class is used to store information about comments made by users on posts,
 * including the content of the comment, the author, and any replies to the comment.
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "content"})
@NoArgsConstructor
@Table(name = "pluma_comment", indexes = {
        @Index(name = "idx_comment_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_post", columnList = "post_id"),
        @Index(name = "idx_comment_parent_id", columnList = "parent_id")
})
public class Comment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pluma_comment_like",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();
    private Integer likedCount = 0;
    @OneToMany(mappedBy = "parentComment",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    /**
     * Constructor for creating a Comment entity with mandatory fields.
     *
     * @param content the content of the comment
     * @param author  the user who authored the comment
     */
    public Comment(String content, User author) {
        this.content = content;
        this.author = author;
    }

    /**
     * Sets the users who liked this comment and updates the like count.
     *
     * @param likedBy the set of users who liked this comment
     */
    public void setLikedBy(Set<User> likedBy) {
        this.likedBy = likedBy;
        this.likedCount = likedBy.size();
    }
}
