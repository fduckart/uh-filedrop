package edu.hawaii.its.filedrop.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
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
