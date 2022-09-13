package edu.hawaii.its.filedrop.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.ldap.core.support.LdapContextSource;

@Profile(value = { "localhost", "test", "prod" })
@Configuration
@PropertySources({
        @PropertySource("classpath:custom.properties"),
        @PropertySource(value = "file:${user.home}/.${user.name}-conf/filedrop-overrides.properties",
                ignoreResourceNotFound = true)
})
public class LdapConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.ldap.context-source")
    public LdapContextSource ldapContextSource() {
        return new LdapContextSource();
    }

}
