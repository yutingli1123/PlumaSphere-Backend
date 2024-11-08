package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_likes")
public class Like {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    private Set<User> user = new HashSet<>();

    @OneToOne
    private Post post;

    @OneToOne
    private Comment comment;
}
