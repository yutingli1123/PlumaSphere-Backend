package fans.goldenglow.plumaspherebackend.entity;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    @Column(unique = true)
    private String username;
    @NotEmpty
    private String password;
    private String name;
    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRoles role;
    private String email;
    private Date dob;
    private String bio;
    private String iconUrl;

    @OneToMany
    private Set<Post> posts = new HashSet<>();
    @OneToMany
    private Set<Comment> comments = new HashSet<>();


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
