package fans.goldenglow.plumaspherebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class to enable JPA auditing.
 * This class is used to automatically populate audit fields like createdDate and lastModifiedDate.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
