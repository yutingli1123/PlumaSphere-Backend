package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "name"})
@NoArgsConstructor
@Table(name = "pluma_tag", indexes = @Index(name = "idx_tag_name", columnList = "name", unique = true))
public class Tag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
