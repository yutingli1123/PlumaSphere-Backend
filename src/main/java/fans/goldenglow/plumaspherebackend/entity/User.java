package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String username;
    private String email;
    private String password;
    private Date dob;


    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }
}
