package fans.goldenglow.plumaspherebackend.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
public class TestSecurityConfig {
}
