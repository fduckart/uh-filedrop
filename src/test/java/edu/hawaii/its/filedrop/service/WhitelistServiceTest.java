package edu.hawaii.its.filedrop.service;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Whitelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class WhitelistServiceTest {

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private Scheduler scheduler;

    @Value("${app.whitelist.check.threshold}")
    private Integer threshold;

    @Test
    public void addWhitelistTest() {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry("Test Entry");
        whitelist.setRegistrant("Some Person");
        whitelist.setCheck(0);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);

        whitelistService.addWhitelist(whitelist);
        whitelist = whitelistService.getWhiteList(whitelist.getId());
        assertNotNull(whitelist);
        assertEquals("Test Entry", whitelist.getEntry());
        assertEquals("Some Person", whitelist.getRegistrant());
        assertEquals(Integer.valueOf(0), whitelist.getCheck());
        assertFalse(whitelist.isExpired());
        assertEquals(2019, whitelist.getCreated().getYear());
        assertTrue(whitelistService.isWhitelisted("Test Entry"));
    }

    @Test
    public void addWhitelistLdapTest() {
        Whitelist whitelist = whitelistService.addWhitelist(new LdapPersonEmpty(), new LdapPersonEmpty());
        whitelist = whitelistService.getWhiteList(whitelist.getId());
        assertNotNull(whitelist);
        assertEquals("", whitelist.getEntry());
        assertEquals("", whitelist.getRegistrant());
        assertEquals(Integer.valueOf(0), whitelist.getCheck());
        assertFalse(whitelist.isExpired());
        assertEquals(2019, whitelist.getCreated().getYear());
    }

    @Test
    public void addCheckTest() {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry("New Entry");
        whitelist.setRegistrant("New Person");
        whitelist.setCheck(0);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);

        whitelist = whitelistService.addWhitelist(whitelist);

        assertNotNull(whitelist);

        int check = whitelistService.addCheck(whitelist, 2);

        assertEquals(2, check);
    }

    @Test
    public void addCheckThresholdTest() {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry("New Person");
        whitelist.setRegistrant("Same Old Mistakes");
        whitelist.setCheck(0);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);

        whitelist = whitelistService.addWhitelist(whitelist);

        assertNotNull(whitelist);

        Integer check = whitelistService.addCheck(whitelist, threshold);

        assertEquals(threshold, check);
        assertTrue(whitelist.isExpired());
    }

    @Test
    public void schedulerTest() throws SchedulerException, InterruptedException {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry("Tame Impala");
        whitelist.setRegistrant("Kevin Parker");
        whitelist.setCheck(threshold);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);
        whitelist = whitelistService.addWhitelist(whitelist);
        scheduler.triggerJob(whitelistService.getJobKey());
        Thread.sleep(500);
        whitelist = whitelistService.getWhiteList(whitelist.getId());
        assertTrue(whitelist.isExpired());

    }
}
