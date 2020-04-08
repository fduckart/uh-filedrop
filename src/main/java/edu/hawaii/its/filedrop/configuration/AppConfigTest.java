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
import org.springframework.util.Assert;

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
        Assert.isTrue(springDatasourceInitialize.equals("never"),
                "Property 'spring.datasource.initialization-mode' should be never.");
        Assert.isTrue(hibernateDdlAuto.equals("none"),
                "Property 'spring.jpa.hibernate.ddl-auto' should be none.");
    }

    @Bean
    @ConfigurationProperties(prefix = "app.ldap.context-source")
    public LdapContextSource ldapContextSource() {
        return new LdapContextSource();
    }
}
