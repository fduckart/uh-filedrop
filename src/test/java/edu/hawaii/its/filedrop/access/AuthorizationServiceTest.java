package edu.hawaii.its.filedrop.access;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ADMINISTRATOR;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ANONYMOUS;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.APPLICANT;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.COORDINATOR;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.REVIEWER;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.SUPERUSER;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.UH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.ApplicationService;
import edu.hawaii.its.filedrop.type.Role.SecurityRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class AuthorizationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Test
    public void basics() {
        assertNotNull(authorizationService);
    }

    @Test
    public void fetch() {
        SecurityRoleHolder roleHolder = authorizationService.fetchRoles("17958670");
        assertThat(roleHolder.size(), equalTo(3));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertTrue(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertTrue(roleHolder.contains(ADMINISTRATOR));
        assertFalse(roleHolder.contains(APPLICANT));
        assertFalse(roleHolder.contains(REVIEWER));

        roleHolder = new SecurityRoleHolder();
        Set<SecurityRole> roles = applicationService.findSystemRoles("17958670");
        for (SecurityRole r : roles) {
            roleHolder.add(r);
        }
        assertThat(roleHolder.size(), equalTo(2));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertFalse(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(APPLICANT));
        assertFalse(roleHolder.contains(REVIEWER));
        assertTrue(roleHolder.contains(ADMINISTRATOR));
        assertTrue(roleHolder.contains(SUPERUSER));

        roleHolder = authorizationService.fetchRoles("10000002");
        assertThat(roleHolder.size(), equalTo(2));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertTrue(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertTrue(roleHolder.contains(ADMINISTRATOR));

        roleHolder = authorizationService.fetchRoles("10000004");
        assertThat(roleHolder.size(), equalTo(1));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertTrue(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(ADMINISTRATOR));

        roleHolder = authorizationService.fetchRoles("10000005");
        assertThat(roleHolder.size(), equalTo(1));
        assertFalse(roleHolder.contains(ANONYMOUS));

        roleHolder = authorizationService.fetchRoles("90000009");
        assertThat(roleHolder.size(), equalTo(1));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertTrue(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(ADMINISTRATOR));

        roleHolder = authorizationService.fetchRoles("10000006", false);
        assertThat(roleHolder.size(), equalTo(1));
        assertTrue(roleHolder.contains(ANONYMOUS));
        assertFalse(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(ADMINISTRATOR));

        roleHolder = new SecurityRoleHolder();
        roles = applicationService.findSystemRoles("10000002");
        for (SecurityRole r : roles) {
            roleHolder.add(r);
        }
        assertThat(roleHolder.size(), equalTo(1));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertFalse(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(APPLICANT));
        assertFalse(roleHolder.contains(REVIEWER));
        assertTrue(roleHolder.contains(ADMINISTRATOR));
        assertFalse(roleHolder.contains(SUPERUSER));

        roleHolder = new SecurityRoleHolder();
        roles = applicationService.findSystemRoles("10000003");
        for (SecurityRole r : roles) {
            roleHolder.add(r);
        }
        assertThat(roleHolder.size(), equalTo(1));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertFalse(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(APPLICANT));
        assertFalse(roleHolder.contains(REVIEWER));
        assertTrue(roleHolder.contains(ADMINISTRATOR));
        assertFalse(roleHolder.contains(SUPERUSER));

        roleHolder = new SecurityRoleHolder();
        roles = applicationService.findSystemRoles("17958670");
        for (SecurityRole r : roles) {
            roleHolder.add(r);
        }
        assertThat(roleHolder.size(), equalTo(2));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertFalse(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(APPLICANT));
        assertFalse(roleHolder.contains(REVIEWER));
        assertTrue(roleHolder.contains(ADMINISTRATOR));
        assertTrue(roleHolder.contains(SUPERUSER));

        // Non-existent uhuuid.
        roles = applicationService.findSystemRoles("ABCDEFGH");
        roleHolder = new SecurityRoleHolder();
        for (SecurityRole r : roles) {
            roleHolder.add(r);
        }
        assertThat(roleHolder.size(), equalTo(0));
        assertFalse(roleHolder.contains(ANONYMOUS));
        assertFalse(roleHolder.contains(UH));
        assertFalse(roleHolder.contains(COORDINATOR));
        assertFalse(roleHolder.contains(APPLICANT));
        assertFalse(roleHolder.contains(REVIEWER));
        assertFalse(roleHolder.contains(ADMINISTRATOR));
        assertFalse(roleHolder.contains(SUPERUSER));
    }
}
