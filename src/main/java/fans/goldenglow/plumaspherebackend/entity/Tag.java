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
@Table(name = "pluma_tag")
public class Tag {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToMany
    private Set<Post> posts = new HashSet<>();
}
