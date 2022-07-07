package edu.hawaii.its.filedrop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Allowlist;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AllowlistRepositoryTest {

    @Autowired
    private AllowlistRepository allowlistRepository;

    @Test
    public void findOne() {
        assertNotNull(allowlistRepository);

        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("Test");
        allowlist.setRegistrant("Person");
        allowlist.setCheck(0);
        LocalDateTime localDateTime = LocalDateTime.now();
        allowlist.setCreated(localDateTime);
        allowlist.setExpired(false);

        allowlistRepository.save(allowlist);

        Allowlist foundAllowlist = allowlistRepository.findById(3).orElse(null);
        assertNotNull(foundAllowlist);
        assertEquals(allowlist.getId(), foundAllowlist.getId());
        assertEquals("Test", foundAllowlist.getEntry());
        assertEquals("Person", foundAllowlist.getRegistrant());
        assertEquals(Integer.valueOf(0), foundAllowlist.getCheck());
        assertFalse(foundAllowlist.isExpired());
        assertEquals(localDateTime, foundAllowlist.getCreated());

        foundAllowlist = allowlistRepository.findByEntry("Test");
        assertNotNull(foundAllowlist);
    }

    @Test
    public void notAllowlistedTest() {
        assertNull(allowlistRepository.findByEntry("testing"));
    }

}
