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
import edu.hawaii.its.filedrop.job.SubmitJob;
import edu.hawaii.its.filedrop.repository.WhitelistRepository;
import edu.hawaii.its.filedrop.type.Whitelist;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class WhitelistServiceTest {

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private WhitelistRepository whitelistRepository;

    @Autowired
    private Scheduler scheduler;

    @Value("${app.whitelist.check.threshold}")
    private Integer threshold;

    @Test
    public void whitelistTest() {
        Whitelist whitelist = new Whitelist();
        whitelist.setId(999);
        whitelist.setEntry("Test Entry");
        whitelist.setRegistrant("Some Person");
        whitelist.setCheck(0);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);
        assertEquals(Integer.valueOf(999), whitelist.getId());
    }

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
        Whitelist whitelist2 = whitelistService.findWhiteList(whitelist.getId());
        assertNotNull(whitelist);
        assertEquals("Test Entry", whitelist.getEntry());
        assertEquals("Some Person", whitelist.getRegistrant());
        assertEquals(Integer.valueOf(0), whitelist.getCheck());
        assertFalse(whitelist.isExpired());
        assertEquals(2019, whitelist.getCreated().getYear());
        assertEquals(whitelist.toString(), whitelist2.toString());
        assertTrue(whitelistService.isWhitelisted("Test Entry"));
    }

    @Test
    public void addWhitelistLdapTest() {
        Whitelist whitelist = whitelistService.addWhitelist(new LdapPersonEmpty(), new LdapPersonEmpty());
        whitelist = whitelistService.findWhiteList(whitelist.getId());
        assertNotNull(whitelist);
        assertEquals("", whitelist.getEntry());
        assertEquals("", whitelist.getRegistrant());
        assertEquals(Integer.valueOf(0), whitelist.getCheck());
        assertFalse(whitelist.isExpired());
        assertEquals(2019, whitelist.getCreated().getYear());
        assertTrue(whitelistService.isWhitelisted(whitelist.getEntry()));
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
        assertTrue(whitelistService.isWhitelisted(whitelist.getEntry()));
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
        assertTrue(whitelistService.isWhitelisted("New Person"));
    }

    @Test
    public void schedulerCheckTest() throws SchedulerException, InterruptedException {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry("Gavin Dance");
        whitelist.setRegistrant("Jon Mess");
        whitelist.setCheck(0);
        LocalDate localDate = LocalDate.now();
        whitelist.setCreated(localDate);
        whitelist.setExpired(false);
        whitelist = whitelistService.addWhitelist(whitelist);
        scheduler.triggerJob(whitelistService.getJobKey());
        Thread.sleep(500);
        whitelist = whitelistService.findWhiteList(whitelist.getId());
        assertEquals(Integer.valueOf(1), whitelist.getCheck());
        assertTrue(whitelistService.isWhitelisted(whitelist.getEntry()));
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
        whitelist = whitelistService.findWhiteList(whitelist.getId());
        assertTrue(whitelistService.isWhitelisted(whitelist.getEntry()));
        assertTrue(whitelist.isExpired());
    }

    @Test
    public void notWhitelistedTest() {
        assertNull(whitelistRepository.findByEntry("testing"));
        assertFalse(whitelistService.isWhitelisted("testing"));
    }
}
