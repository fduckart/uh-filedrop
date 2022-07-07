package edu.hawaii.its.filedrop.access;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.controller.WithMockUhUser;
import edu.hawaii.its.filedrop.type.Role.SecurityRole;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UserContextServiceTest {

    @Autowired
    private UserContextService userContextService;

    @Test
    @WithMockUhUser(username = "admin", roles = { "ROLE_ADMINISTRATOR" })
    public void basics() {
        assertThat(userContextService.getCurrentUhuuid(), equalTo("12345678"));
        assertThat(userContextService.getCurrentUsername(), equalTo("admin"));
        assertThat(userContextService.toString(), startsWith("UserContextServiceImpl"));

        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        assertThat(user.getUhuuid(), equalTo("12345678"));
        assertThat(user.getUsername(), equalTo("admin"));
        assertTrue(user.hasRole(SecurityRole.ADMINISTRATOR));
    }

    @Test
    @WithAnonymousUser
    public void anonymousUser() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        assertThat(user.getUhuuid(), equalTo(""));
        assertThat(user.getUsername(), equalTo("anonymous"));
        assertTrue(user.hasRole(SecurityRole.ANONYMOUS));
        assertFalse(user.hasRole(SecurityRole.UH));
    }

    @Test
    public void nonUser() {
        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        assertThat(user.getUhuuid(), equalTo(""));
        assertThat(user.getUsername(), equalTo("anonymous"));
        assertThat(user.getAuthorities().size(), equalTo(1));
        assertTrue(user.hasRole(SecurityRole.ANONYMOUS));
    }
}
