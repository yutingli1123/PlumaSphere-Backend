package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_likes")
public class Like {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    private List<User> user;

    @OneToOne
    private Post post;

    @OneToOne
    private Comment comment;
}
