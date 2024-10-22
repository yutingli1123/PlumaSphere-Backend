package fans.goldenglow.plumaspherebackend.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fans.goldenglow.plumaspherebackend.config.filter.ApiKeyAuthFilter;
import fans.goldenglow.plumaspherebackend.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private SystemConfigService systemConfigService;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager userAuthenticationManager() {
        return authentication -> {
            authentication.setAuthenticated(false);
            String principal = (String) authentication.getPrincipal();
            String[] formattedToken = principal.split(" ");
            if (formattedToken.length == 2 && formattedToken[0].equals("Bearer")) {
                Optional<String> secretKey = systemConfigService.get("secret_key");
                if (secretKey.isPresent()) {
                    String username = JWT.require(Algorithm.HMAC256(secretKey.get())).build().verify(formattedToken[1]).getIssuer();
                    authentication = new PreAuthenticatedAuthenticationToken(username, principal);
                    authentication.setAuthenticated(true);
                }
            }
            return authentication;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(HttpHeaders.AUTHORIZATION);
        filter.setAuthenticationManager(userAuthenticationManager());
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> corsConfigurationSource())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(filter)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/v1/init")
                        .access((authentication, object) -> {
                            Optional<String> result = systemConfigService.get("init_complete");
                            return new AuthorizationDecision(result.isEmpty() || result.get().equals("false"));
                        })
                        .requestMatchers("/api/v1/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                );
        return http.build();
    }
}
