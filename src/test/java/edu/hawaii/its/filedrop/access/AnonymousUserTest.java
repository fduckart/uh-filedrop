package edu.hawaii.its.filedrop.access;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.filedrop.type.Role.SecurityRole;

public class AnonymousUserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new AnonymousUser();
    }

    @Test
    public void testConstructions() {
        assertNotNull(user);
        assertEquals("anonymous", user.getUsername());
        assertEquals("anonymous", user.getUid());
        assertThat(user.getUhUuid(), equalTo(""));
        assertEquals("", user.getPassword());
        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.hasRole(SecurityRole.ANONYMOUS));
    }
}
