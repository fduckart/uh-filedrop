package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Role;
import edu.hawaii.its.filedrop.type.Role.SecurityRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Test
    public void findAll() {
        assertNotNull(roleService);

        final int id = 1;
        Role r0 = roleService.findById(id);
        Role r1 = roleService.findById(id);
        assertEquals(r0, r1);
        assertSame(r0, r1);

        assertEquals(r0, r1);
        assertSame(r0, r1);

        Role r2 = roleService.findById(id);
        assertEquals(r0, r2);
        assertSame(r0, r2);

        Role r3 = roleService.findByRole("APPLICANT");
        assertThat(r3.getRole(), equalTo("APPLICANT"));
        assertThat(r3.getSecurityRole(), equalTo(SecurityRole.APPLICANT.name()));

        Role rn = roleService.findById(666);
        assertNull(rn);

        List<Role> rolesOne = roleService.findAll();
        assertTrue(rolesOne.size() > 0);
        List<Role> rolesTwo = roleService.findAll();
        assertEquals(rolesOne.size(), rolesTwo.size());
        assertSame(rolesOne, rolesTwo);
    }

    @Test
    public void findAllTwo() {
        assertNotNull(roleService);

        long count = roleService.count();
        assertThat(count, equalTo(13L));

        final int id = 1;
        Role r0 = roleService.findById(id);
        Role r1 = roleService.findById(id);
        assertEquals(r0, r1);
        assertSame(r0, r1);

        r1.setDescription(r1.getDescription() + " (updated)");
        roleService.save(r1);

        assertEquals(r0, r1);
        assertSame(r0, r1);

        Role r2 = roleService.findById(id);
        assertEquals(r0, r2);
        assertSame(r0, r2);

        Role r3 = roleService.findByRole("APPLICANT");
        assertThat(r3.getRole(), equalTo("APPLICANT"));

        Role rn = roleService.findById(666);
        assertNull(rn);
    }

    @Test
    public void findBySecurityRole() {
        List<Role> roles = roleService.findBySecurityRole("");
        assertThat(roles.size(), equalTo(0));

        roles = roleService.findBySecurityRole("REVIEWER");
        assertThat(roles.size(), equalTo(8));

        roles = roleService.findBySecurityRole("COORDINATOR");
        assertThat(roles.size(), equalTo(1));

        roles = roleService.findAll();
        for (Role r : roles) {
            String s = r.getSecurityRole();
            List<Role> t = roleService.findBySecurityRole(s);
            assertTrue(t.size() > 0);
        }
    }

    @Test
    public void testExists() {
        List<Role> roles = roleService.findAll();
        for (Role r : roles) {
            String s = r.getSecurityRole();
            assertTrue(roleService.existsBySecurityRole(s));
        }
    }
}
