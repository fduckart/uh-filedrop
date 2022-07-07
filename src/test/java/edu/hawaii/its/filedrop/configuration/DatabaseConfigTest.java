package edu.hawaii.its.filedrop.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class DatabaseConfigTest {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Test
    public void construction() {
        assertThat(databaseConfig, not(equalTo(null)));
    }

}
