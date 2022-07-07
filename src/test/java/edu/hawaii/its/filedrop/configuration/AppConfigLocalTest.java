package edu.hawaii.its.filedrop.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AppConfigLocalTest {

    @Test
    public void construction() {
        AppConfigLocal appConfig = new AppConfigLocal();
        assertNotNull(appConfig);
        appConfig.init();
        assertNotNull(appConfig.ldapContextSource());
    }

}
