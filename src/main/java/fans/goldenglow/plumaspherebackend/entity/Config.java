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
    @JsonIgnore
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    @NotEmpty
    private String configKey;

    @NotEmpty
    private String configValue;

    @JsonIgnore
    private Boolean isOpenToPublic = false;

    public Config(String configKey, String configValue, Boolean isOpenToPublic) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.isOpenToPublic = isOpenToPublic;
    }
}
