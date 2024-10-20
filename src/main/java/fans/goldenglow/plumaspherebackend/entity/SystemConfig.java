package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "system_config")
public class SystemConfig {
    @Id
    @GeneratedValue
    private Long id;

    @NotEmpty
    private String configKey;

    @NotEmpty
    private String configValue;

    public SystemConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }
}
