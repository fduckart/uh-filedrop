package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.filedrop.type.Role.SecurityRole;

public class RoleTest {

    private Role role;

    @BeforeEach
    public void setUp() {
        role = new Role();
    }

    @Test
    public void construction() {
        assertNotNull(role);
    }

    @Test
    public void accessors() {
        Role r1 = new Role();
        r1.setId(5);
        Role r2 = new Role();
        r2.setId(5);
        assertThat(r1, equalTo(r2));
        assertThat(r1.getId(), equalTo(5));
        assertThat(r1.getId(), equalTo(r2.getId()));
        assertThat(r1.hashCode(), equalTo(r2.hashCode()));

        r1.setRole("role");
        assertThat(r1, not(equalTo(r2)));
        assertThat(r2, not(equalTo(r1)));
        r2.setRole("role");
        assertThat(r1, equalTo(r2));
        assertThat(r1.hashCode(), equalTo(r2.hashCode()));

        r1.setDescription("description");
        assertThat(r1, not(equalTo(r2)));
        assertThat(r2, not(equalTo(r1)));
        r2.setDescription("description");
        assertThat(r1, equalTo(r2));
        assertThat(r1.hashCode(), equalTo(r2.hashCode()));

        r1.setDescription(null);
        assertThat(r1, not(equalTo(r2)));
        assertThat(r2, not(equalTo(r1)));
        r2.setDescription(null);
        assertThat(r1, equalTo(r2));
        assertThat(r2, equalTo(r1));

        r1.setRole(null);
        assertThat(r1, not(equalTo(r2)));
        assertThat(r2, not(equalTo(r1)));
        r2.setRole(null);
        assertThat(r1, equalTo(r2));
        assertThat(r2, equalTo(r1));

        r1.setId(null);
        assertThat(r1, not(equalTo(r2)));
        assertThat(r2, not(equalTo(r1)));
        r2.setId(null);
        assertThat(r1, equalTo(r2));
        assertThat(r2, equalTo(r1));
        r2.setId(2);
        assertThat(r1, not(equalTo(r2)));
        assertThat(r2, not(equalTo(r1)));

        assertThat(r1.getSecurityRole(), equalTo(""));
        assertThat(r2.getSecurityRole(), equalTo(""));

        r1.setSecurityRole("r1-security-role");
        assertThat(r1.getSecurityRole(), equalTo("r1-security-role"));
        assertThat(r2.getSecurityRole(), equalTo(""));

        r2.setSecurityRole("r2-security-role");
        assertThat(r1.getSecurityRole(), equalTo("r1-security-role"));
        assertThat(r2.getSecurityRole(), equalTo("r2-security-role"));

        r1 = new Role(1, SecurityRole.ADMINISTRATOR);
        assertThat(r1.getSecurityRole(), equalTo(SecurityRole.ADMINISTRATOR.name()));
        r2 = new Role(2, SecurityRole.ANONYMOUS);
        assertThat(r2.getSecurityRole(), equalTo(SecurityRole.ANONYMOUS.name()));

        r1 = new Role(1, SecurityRole.ADMINISTRATOR);
        assertThat(r1.isAdministrator(), equalTo(true));
        assertThat(r1.isNonUH(), equalTo(false));
        assertThat(r1.isSuperuser(), equalTo(false));

        r1 = new Role(1, SecurityRole.SUPERUSER);
        assertThat(r1.isAdministrator(), equalTo(true));
        assertThat(r1.isNonUH(), equalTo(false));
        assertThat(r1.isSuperuser(), equalTo(true));

        r1 = new Role(1, SecurityRole.NON_UH);
        assertThat(r1.isAdministrator(), equalTo(false));
        assertThat(r1.isNonUH(), equalTo(true));
        assertThat(r1.isSuperuser(), equalTo(false));
    }

    @Test
    public void testHashCode() {
        Role r1 = new Role();
        Role r2 = new Role();
        assertThat(r1.hashCode(), equalTo(r2.hashCode()));
        assertTrue(r1.equals(r2));

        r1 = new Role(1, SecurityRole.UH);
        r2 = new Role(1, SecurityRole.SUPERUSER);
        assertThat(r1.hashCode(), not(equalTo(r2.hashCode())));
        assertFalse(r1.equals(r2));

        r1 = new Role(1);
        r2 = new Role(1, SecurityRole.SUPERUSER);
        assertThat(r1.hashCode(), not(equalTo(r2.hashCode())));
        assertFalse(r1.equals(r2));

        r1 = new Role(1, SecurityRole.SUPERUSER);
        r2 = new Role(1, SecurityRole.SUPERUSER);
        assertThat(r1.hashCode(), equalTo(r2.hashCode()));
        assertTrue(r1.equals(r2));
    }

    @Test
    public void testEquals() {
        Role r1 = new Role();
        assertEquals(r1, r1); // To self.
        assertTrue(r1.equals(r1)); // To self.
        assertFalse(r1.equals(null));
        assertFalse(r1.equals(new String())); // Wrong type.

        Role r2 = new Role();
        assertThat(r1, equalTo(r2));

    }

    @Test
    public void compareTo() {
        Role c0 = new Role(1);
        Role c1 = new Role(1);
        assertThat(c0.compareTo(c1), equalTo(0));

        c1 = new Role(2);
        assertThat(c0.compareTo(c1), equalTo(-1));

        c1 = new Role(0);
        assertThat(c0.compareTo(c1), equalTo(1));
    }

    @Test
    public void testIds() {
        assertThat(SecurityRole.NON_UH.value(), equalTo(1));
    }

    @Test
    public void testToString() {
        assertThat(role.toString(), containsString("id=null, role=null"));

        role.setId(12345);
        assertThat(role.toString(), containsString("Role [id=12345,"));
    }

}
