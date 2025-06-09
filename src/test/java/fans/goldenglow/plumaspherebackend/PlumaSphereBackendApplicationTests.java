package fans.goldenglow.plumaspherebackend;

import fans.goldenglow.plumaspherebackend.config.EmbeddedRedisTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisTestConfiguration.class)
class PlumaSphereBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
