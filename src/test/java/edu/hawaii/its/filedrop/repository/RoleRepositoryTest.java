package edu.hawaii.its.filedrop.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Role;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@TestMethodOrder(MethodOrderer.Random.class)
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

        r1.setDescription(r1.getDescription() + " (updated)");
        roleRepository.save(r1);

        assertNotEquals(r0, r1);

        Role r2 = roleRepository.findById(id).get();
        assertNotEquals(r0, r2);

        Role r3 = roleRepository.findByRole("STAFF");
        assertThat(r3, equalTo(null));

        Role rn = roleRepository.findById(666).orElse(null);
        assertNull(rn);
    }

}
