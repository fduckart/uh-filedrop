package edu.hawaii.its.filedrop.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserSecurityContextFactory.class)
public @interface WithMockUhUser {
    String username() default "user";

    String[] roles() default { "ROLE_UH" };

    String uhuuid() default "12345678";

    String name() default "User";

    String email() default "user@test.edu";

    String affiliation() default "staff";
}
