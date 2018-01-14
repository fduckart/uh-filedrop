package edu.hawaii.its.filedrop.access;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import edu.hawaii.its.filedrop.type.Role.SecurityRole;

public class RoleHolderTest {

    @Test
    public void constructors() {
        SecurityRoleHolder roleHolder = new SecurityRoleHolder();
        assertThat(roleHolder.size(), equalTo(0));

        Set<SecurityRole> roles = null;
        roleHolder = new SecurityRoleHolder(roles);
        assertThat(roleHolder.size(), equalTo(0));

        roles = new LinkedHashSet<>();
        roleHolder = new SecurityRoleHolder(roles);
        assertThat(roleHolder.size(), equalTo(0));

        roles = new LinkedHashSet<>();
        roles.add(SecurityRole.ANONYMOUS);
        roleHolder = new SecurityRoleHolder(roles);
        assertThat(roleHolder.size(), equalTo(1));

        roles = new LinkedHashSet<>();
        roles.add(SecurityRole.ANONYMOUS);
        roles.add(SecurityRole.UH);
        roleHolder = new SecurityRoleHolder(roles);
        assertThat(roleHolder.size(), equalTo(2));

        roles = new LinkedHashSet<>();
        roles.add(SecurityRole.ANONYMOUS);
        roles.add(SecurityRole.UH);
        roles.add(SecurityRole.COORDINATOR);
        roleHolder = new SecurityRoleHolder(roles);
        assertThat(roleHolder.size(), equalTo(3));
    }

    @Test
    public void basics() {
        SecurityRoleHolder roleHolder = new SecurityRoleHolder();
        assertThat(roleHolder.size(), equalTo(0));
        roleHolder.add(SecurityRole.ANONYMOUS);
        assertThat(roleHolder.size(), equalTo(1));
        roleHolder.add(SecurityRole.UH);
        assertThat(roleHolder.size(), equalTo(2));
        roleHolder.add(SecurityRole.COORDINATOR);
        assertThat(roleHolder.size(), equalTo(3));

        assertThat(roleHolder.toString(), containsString("ROLE_ANONYMOUS"));
        assertThat(roleHolder.toString(), containsString("ROLE_UH"));
        assertThat(roleHolder.toString(), containsString("ROLE_COORDINATOR"));
    }

    @Test
    public void contains() {
        SecurityRoleHolder roleHolder = new SecurityRoleHolder();

        assertFalse(roleHolder.contains(SecurityRole.ANONYMOUS));
        roleHolder.add(SecurityRole.ANONYMOUS);
        assertTrue(roleHolder.contains(SecurityRole.ANONYMOUS));

        assertFalse(roleHolder.contains(SecurityRole.UH));
        roleHolder.add(SecurityRole.UH);
        assertTrue(roleHolder.contains(SecurityRole.UH));

        assertFalse(roleHolder.contains(SecurityRole.COORDINATOR));
        roleHolder.add(SecurityRole.COORDINATOR);
        assertTrue(roleHolder.contains(SecurityRole.COORDINATOR));
    }
}
