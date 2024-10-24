package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private List<Post> posts = new ArrayList<>();
}
