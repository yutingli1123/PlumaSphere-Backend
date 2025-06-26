package fans.goldenglow.plumaspherebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a tag in the PlumaSphere application.
 * This class is used to store information about tags that can be associated with posts,
 * allowing for categorization and easier searching of content.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"name"})
@ToString(of = {"id", "name"})
@NoArgsConstructor
@Table(name = "pluma_tag", indexes = @Index(name = "idx_tag_name", columnList = "name", unique = true))
public class Tag implements Serializable {
    @Serial
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @Version
    @JsonIgnore
    private Long version;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Tag name cannot be blank")
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }
}
