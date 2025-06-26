package fans.goldenglow.plumaspherebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;

/**
 * Entity representing a configuration setting.
 * This class is used to store key-value pairs for application configuration.
 */
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

    /**
     * Constructor for creating a Config entity with mandatory fields.
     *
     * @param configKey   the key of the configuration
     * @param configValue the value of the configuration
     */
    public Config(String configKey, String configValue, Boolean isOpenToPublic) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.isOpenToPublic = isOpenToPublic;
    }
}
