package edu.hawaii.its.filedrop.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.support.LdapContextSource;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AppConfigLocalTest {

    @Autowired
    private LdapContextSource ldapContextSource;

    @Test
    public void construction() {
        AppConfigLocal appConfig = new AppConfigLocal();
        assertNotNull(appConfig);
        appConfig.init();

        assertNotNull(ldapContextSource);
    }

}
