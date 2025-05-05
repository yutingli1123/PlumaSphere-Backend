package fans.goldenglow.plumaspherebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "username", "nickname", "dob"})
@NoArgsConstructor
@Table(name = "pluma_user", indexes = @Index(name = "idx_user_username", columnList = "username", unique = true))
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    private String nickname;
    private String bio;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoles role;
    private LocalDate dob;
    private String avatarUrl;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(updatable = false)
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "author", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();
    @OneToMany(mappedBy = "author", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
