package fans.goldenglow.plumaspherebackend.config;

import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityService securityService;
    private final ConfigService configService;

    @Autowired
    public SecurityConfig(SecurityService securityService, ConfigService configService) {
        this.securityService = securityService;
        this.configService = configService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/init")
                        .access(((authentication, object) -> {
                            Optional<String> result = configService.get("initialled");
                            return new AuthorizationDecision(result.isEmpty());
                        }))
                        .requestMatchers("/api/v1/login", "/api/v1/status", "/public/**", "/error/**").permitAll()
                        .anyRequest().access(hasScope("admin"))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(securityService.getSecret()).build();
    }
}