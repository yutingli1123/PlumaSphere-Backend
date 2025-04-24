package fans.goldenglow.plumaspherebackend.entity;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @NotEmpty
    @NonNull
    @Column(unique = true)
    private String username;
    private String password;
    private String nickname;
    @NonNull
    @Enumerated(EnumType.STRING)
    private UserRoles role;
    private Date dob;
    private String avatarUrl;

    @OneToMany
    private Set<Post> posts = new HashSet<>();
    @OneToMany
    private Set<Comment> comments = new HashSet<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
