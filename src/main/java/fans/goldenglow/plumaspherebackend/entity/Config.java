package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_config")
public class Config {
    @Id
    @GeneratedValue
    private Long id;

    @NotEmpty
    private String configKey;

    @NotEmpty
    private String configValue;

    public Config(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }
}
