package edu.hawaii.its.filedrop.repository;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Whitelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class WhitelistRepositoryTest {

    @Autowired
    private WhitelistRepository whitelistRepository;

    @Test
    public void findOne() {
        assertNotNull(whitelistRepository);

        Whitelist whitelist = new Whitelist();
        whitelist.setEntry("Test");
        whitelist.setRegistrant("Person");
        whitelist.setCheck(0);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);

        whitelistRepository.save(whitelist);

        Whitelist foundWhitelist = whitelistRepository.findById(1).orElse(null);
        assertNotNull(foundWhitelist);
        assertEquals(whitelist.getId(), foundWhitelist.getId());
        assertEquals("Test", foundWhitelist.getEntry());
        assertEquals("Person", foundWhitelist.getRegistrant());
        assertEquals(Integer.valueOf(0), foundWhitelist.getCheck());
        assertFalse(foundWhitelist.isExpired());
        assertEquals(localDate, foundWhitelist.getCreated());
    }

}