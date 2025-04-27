package fans.goldenglow.plumaspherebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "configKey", "configValue"})
@NoArgsConstructor
@Table(name = "pluma_config")
public class Config implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @NotEmpty
    private String configKey;

    @NotEmpty
    private String configValue;

    @JsonIgnore
    private Boolean isOpenToPublic = false;

    public Config(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }
}
