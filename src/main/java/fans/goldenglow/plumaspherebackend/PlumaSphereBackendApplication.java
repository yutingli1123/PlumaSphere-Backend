package fans.goldenglow.plumaspherebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableAspectJAutoProxy
public class PlumaSphereBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlumaSphereBackendApplication.class, args);
    }

}
