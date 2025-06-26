package fans.goldenglow.plumaspherebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the PlumaSphere backend.
 * This class is responsible for bootstrapping the Spring Boot application.
 * It enables scheduling and aspect-oriented programming features.
 */
@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
public class PlumaSphereBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlumaSphereBackendApplication.class, args);
    }

}
