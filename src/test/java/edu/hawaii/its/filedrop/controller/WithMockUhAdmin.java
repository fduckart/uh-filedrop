package edu.hawaii.its.filedrop.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAdminSecurityContextFactory.class)
public @interface WithMockUhAdmin {
    String username() default "admin";

    String[] roles() default { "ROLE_UH", "ROLE_ADMINISTRATOR" };

    String uhuuid() default "12345679";

    String name() default "Admin";

    String email() default "admin@test.edu";

    String affiliation() default "staff";
}
