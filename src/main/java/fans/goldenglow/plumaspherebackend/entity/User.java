package fans.goldenglow.plumaspherebackend.entity;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @NotEmpty
    @Column(unique = true)
    private String username;
    private String email;
    @NotEmpty
    private String password;
    private Date dob;
    @NotEmpty
    @Enumerated(EnumType.STRING)
    private UserRoles role;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
