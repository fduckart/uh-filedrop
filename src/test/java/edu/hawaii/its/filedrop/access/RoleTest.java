package edu.hawaii.its.filedrop.access;

import org.junit.Test;

import edu.hawaii.its.filedrop.type.Role.SecurityRole;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ADMINISTRATOR;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ANONYMOUS;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.NON_UH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class RoleTest {

    @Test
    public void longName() {
        for (SecurityRole role : SecurityRole.values()) {
            assertEquals("ROLE_" + role.name(), role.longName());
        }
    }

    @Test
    public void find() {
        SecurityRole role = SecurityRole.find(SecurityRole.ADMINISTRATOR.name());
        assertNotNull(role);
        assertThat(role.name(), equalTo(ADMINISTRATOR.name()));
        assertThat(role.longName(), equalTo(ADMINISTRATOR.longName()));
        assertThat(role.toString(), equalTo("ROLE_ADMINISTRATOR"));
        role = SecurityRole.find("non-existent-role");
        assertNull(role);
    }

    @Test
    public void value() {
        SecurityRole anonymous = ANONYMOUS;
        assertThat(anonymous.value(), equalTo(0));

        SecurityRole applicant = NON_UH;
        assertThat(applicant.value(), equalTo(1));

        SecurityRole administator = ADMINISTRATOR;
        assertThat(administator.value(), equalTo(13));
    }
}
