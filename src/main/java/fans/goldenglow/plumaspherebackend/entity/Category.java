package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_category")
public class Category {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany
    private Set<Post> posts = new HashSet<>();
}
