package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Role;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Test
    public void findAll() {
        assertNotNull(roleService);

        final int id = 1;
        Role r0 = roleService.find(id);
        Role r1 = roleService.find(id);
        assertEquals(r0, r1);
        assertSame(r0, r1);

        assertEquals(r0, r1);
        assertSame(r0, r1);

        Role r2 = roleService.find(id);
        assertEquals(r0, r2);
        assertSame(r0, r2);

        Role r3 = roleService.findByRole("FACULTY");
        assertThat(r3.getRole(), equalTo("FACULTY"));
        assertThat(r3.getShortDescription(), equalTo("Faculty"));
        assertThat(r3.getDescription(), equalTo("Faculty"));

        Role rn = roleService.find(666);
        assertNull(rn);
    }

}
