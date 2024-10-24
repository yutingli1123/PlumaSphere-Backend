package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private List<Post> posts = new ArrayList<>();
}
