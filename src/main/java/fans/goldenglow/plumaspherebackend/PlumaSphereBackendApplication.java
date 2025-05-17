package fans.goldenglow.plumaspherebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlumaSphereBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlumaSphereBackendApplication.class, args);
    }

}
