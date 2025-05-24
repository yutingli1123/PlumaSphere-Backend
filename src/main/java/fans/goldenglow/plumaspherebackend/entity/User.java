package fans.goldenglow.plumaspherebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fans.goldenglow.plumaspherebackend.constant.AvatarColor;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String avatarColor;
    private String initials;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    public User(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    public User(String username, String password) {
        this.username = username;
        this.nickname = Arrays.stream(username.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
        this.role = UserRoles.REGULAR;
        this.password = password;
        setInitials();
    }

    private void setInitials() {
        if (nickname == null || nickname.isEmpty()) return;

        String[] parts = nickname.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        this.initials = initials.toString();
        this.avatarColor = AvatarColor.getRandomColor().getHex();
    }
}
