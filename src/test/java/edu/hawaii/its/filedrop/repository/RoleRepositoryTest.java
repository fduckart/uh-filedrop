package edu.hawaii.its.filedrop.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Role;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void findAll() {
        assertNotNull(roleRepository);

        long count = roleRepository.count();
        assertThat(count, equalTo(3L));

        final int id = 1;
        Role r0 = roleRepository.findById(id).get();
        Role r1 = roleRepository.findById(id).get();
        assertEquals(r0, r1);
        assertSame(r0, r1);

        r1.setDescription(r1.getDescription() + " (updated)");
        roleRepository.save(r1);

        assertEquals(r0, r1);
        assertSame(r0, r1);

        Role r2 = roleRepository.findById(id).get();
        assertEquals(r0, r2);
        assertSame(r0, r2);

        Role r3 = roleRepository.findByRole("STAFF");
        assertThat(r3, equalTo(null));
        ///assertThat(r3.getRole(), equalTo("STAFF"));

        Role rn = roleRepository.findById(666).orElse(null);
        assertNull(rn);
    }

}
