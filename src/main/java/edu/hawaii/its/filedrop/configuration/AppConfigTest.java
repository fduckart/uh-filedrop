package edu.hawaii.its.filedrop.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.ldap.core.support.LdapContextSource;

import edu.hawaii.its.filedrop.exception.PropertyNotSetException;

@Profile(value = { "test", "prod" })
@Configuration(value = "appConfig")
@ComponentScan(basePackages = "edu.hawaii.its.filedrop")
@EnableJpaRepositories(basePackages = { "edu.hawaii.its.filedrop.repository" })
@PropertySources({
        @PropertySource("classpath:custom.properties"),
        @PropertySource(value = "file:${user.home}/.${user.name}-conf/filedrop-overrides.properties",
                ignoreResourceNotFound = true)
})
public class AppConfigTest extends AppConfig {

    @Value("${app.datasource.initialization-mode:}")
    private String springDatasourceInitialize = "";

    @Value("${app.jpa.hibernate.ddl-auto:}")
    private String hibernateDdlAuto = "";

    @Override
    @PostConstruct
    public void init() {
        logger.info("AppConfigRun init");
        if (!springDatasourceInitialize.equals("never")) {
            throw new PropertyNotSetException("spring.datasource.initialization-mode", "never", springDatasourceInitialize);
        }

        if (!hibernateDdlAuto.equals("none")) {
            throw new PropertyNotSetException("spring.jpa.hibernate.ddl-auto", "none", hibernateDdlAuto);
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "app.ldap.context.source")
    public LdapContextSource ldapContextSource() {
        return new LdapContextSource();
    }
}
