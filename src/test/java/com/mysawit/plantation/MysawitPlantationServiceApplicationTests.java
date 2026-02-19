package com.mysawit.plantation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MysawitPlantationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainRunsWithTestProfile() {
        MysawitPlantationServiceApplication.main(
            new String[] {
                "--spring.profiles.active=test",
                "--spring.main.web-application-type=none",
                "--app.test.close-context=true"
            }
        );
    }
}
