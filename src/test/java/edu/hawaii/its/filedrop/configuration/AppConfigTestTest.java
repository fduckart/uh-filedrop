package edu.hawaii.its.filedrop.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AppConfigTestTest {

    @Autowired
    @Qualifier("appConfig")
    private AppConfig appConfig;

    @Test
    public void construction() {
        assertThat(appConfig, not(equalTo(null)));
    }

    @Test
    public void init() {
        appConfig.init();
    }
}
