package fans.goldenglow.plumaspherebackend.entity;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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
    @NotEmpty
    @Enumerated(EnumType.STRING)
    private UserRoles role;
    private String email;
    private Date dob;
    private String bio;
    private String iconUrl;

    @OneToMany

    private List<Post> posts = new ArrayList<>();
    @OneToMany
    private List<Comment> comments = new ArrayList<>();


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
