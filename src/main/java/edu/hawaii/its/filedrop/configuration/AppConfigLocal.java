package edu.hawaii.its.filedrop.configuration;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.ldap.core.support.LdapContextSource;

@Profile(value = { "localhost" })
@Configuration(value = "appConfig")
@ComponentScan(basePackages = "edu.hawaii.its.filedrop")
@EnableJpaRepositories(basePackages = { "edu.hawaii.its.filedrop.repository" })
@PropertySources({
        @PropertySource("classpath:custom.properties"),
        @PropertySource(value = "file:${user.home}/.${user.name}-conf/filedrop-overrides.properties",
                ignoreResourceNotFound = true)
})
public class AppConfigLocal extends AppConfig {

    @Override
    @PostConstruct
    public void init() {
        logger.info("AppConfig init");
    }

    @Bean
    @ConfigurationProperties(prefix = "app.ldap.context-source")
    public LdapContextSource ldapContextSource() {
        return new LdapContextSource();
    }

}
