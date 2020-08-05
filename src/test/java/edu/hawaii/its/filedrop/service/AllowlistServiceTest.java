package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.AllowlistRepository;
import edu.hawaii.its.filedrop.type.Allowlist;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AllowlistServiceTest {

    @Autowired
    private AllowlistService allowlistService;

    @Autowired
    private AllowlistRepository allowlistRepository;

    @Autowired
    private SchedulerService schedulerService;

    @Value("${app.job.allowlist.threshold}")
    private Integer threshold;

    @Autowired
    private Scheduler scheduler;

    @Test
    public void allowlistTest() {
        Allowlist allowlist = new Allowlist();
        allowlist.setId(999);
        allowlist.setEntry("Test Entry");
        allowlist.setRegistrant("Some Person");
        allowlist.setCheck(0);
        LocalDateTime localDate = LocalDateTime.of(2019, 12, 31, 0, 0, 0);
        allowlist.setCreated(localDate);
        allowlist.setExpired(false);
        assertEquals(Integer.valueOf(999), allowlist.getId());
    }

    @Test
    public void addAllowlistTest() {
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("Test Entry");
        allowlist.setRegistrant("Some Person");
        allowlist.setCheck(0);
        LocalDateTime localDate = LocalDateTime.of(2019, 12, 31, 0, 0, 0);
        allowlist.setCreated(localDate);
        allowlist.setExpired(false);
        allowlistService.addAllowlist(allowlist);
        Allowlist allowlist2 = allowlistService.findAllowList(allowlist.getId());
        assertNotNull(allowlist);
        assertEquals("Test Entry", allowlist.getEntry());
        assertEquals("Some Person", allowlist.getRegistrant());
        assertEquals(Integer.valueOf(0), allowlist.getCheck());
        assertFalse(allowlist.isExpired());
        assertEquals(2019, allowlist.getCreated().getYear());
        assertEquals(allowlist.toString(), allowlist2.toString());
        assertTrue(allowlistService.isAllowlisted("Test Entry"));
    }

    @Test
    public void addAllowlistLdapTest() {
        Allowlist allowlist = allowlistService.addAllowlist(new LdapPersonEmpty(), new LdapPersonEmpty());
        allowlist.setCreated(LocalDateTime.of(2019, 12, 31, 0, 0, 0));
        allowlist = allowlistService.addAllowlist(allowlist);
        allowlist = allowlistService.findAllowList(allowlist.getId());
        assertNotNull(allowlist);
        assertEquals("", allowlist.getEntry());
        assertEquals("", allowlist.getRegistrant());
        assertEquals(Integer.valueOf(0), allowlist.getCheck());
        assertFalse(allowlist.isExpired());
        assertEquals(2019, allowlist.getCreated().getYear());
        assertTrue(allowlistService.isAllowlisted(allowlist.getEntry()));
    }

    @Test
    public void addCheckTest() {
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("New Entry");
        allowlist.setRegistrant("New Person");
        allowlist.setCheck(0);
        LocalDateTime localDate = LocalDateTime.now();
        allowlist.setCreated(localDate);
        allowlist.setExpired(false);

        allowlist = allowlistService.addAllowlist(allowlist);

        assertNotNull(allowlist);

        int check = allowlistService.addCheck(allowlist, 2);

        assertEquals(2, check);
        assertTrue(allowlistService.isAllowlisted(allowlist.getEntry()));
    }

    @Test
    public void addCheckThresholdTest() {
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("jwlennon");
        allowlist.setRegistrant("help");
        allowlist.setCheck(0);
        LocalDateTime localDate = LocalDateTime.of(2019, 12, 31, 0, 0, 0);
        allowlist.setCreated(localDate);
        allowlist.setExpired(false);

        allowlist = allowlistService.addAllowlist(allowlist);

        assertNotNull(allowlist);

        Integer check = allowlistService.addCheck(allowlist, threshold);

        assertEquals(threshold, check);
        assertTrue(allowlist.isExpired());
        assertTrue(allowlistService.isAllowlisted("jwlennon"));

        allowlistService.checkAllowlists();

        allowlist = allowlistService.findAllowList(allowlist.getId());
        assertThat(allowlist.getCheck(), equalTo(0));
        assertFalse(allowlist.isExpired());
    }

    @Test
    public void schedulerCheckTest() {
        long count = allowlistService.recordCount();
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("Gavin Dance");
        allowlist.setRegistrant("Jon Mess");
        allowlist.setCheck(0);
        LocalDateTime localDate = LocalDateTime.of(2019, 12, 31, 0, 0, 0);
        allowlist.setCreated(localDate);
        allowlist.setExpired(false);
        allowlist = allowlistService.addAllowlist(allowlist);
        allowlistService.checkAllowlists();
        allowlist = allowlistService.findAllowList(allowlist.getId());
        assertThat(allowlist.getCheck(), greaterThanOrEqualTo(1));
        assertTrue(allowlistService.isAllowlisted(allowlist.getEntry()));
        allowlistService.deleteAllowlist(allowlist);
        assertThat(allowlistService.recordCount(), greaterThanOrEqualTo(count - 1L));
    }

    @Test
    public void schedulerTest() throws SchedulerException {
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry("Tame Impala");
        allowlist.setRegistrant("Kevin Parker");
        allowlist.setCheck(threshold);
        LocalDateTime localDate = LocalDateTime.of(2019, 12, 31, 0, 0, 0);
        allowlist.setCreated(localDate);
        allowlist.setExpired(false);
        allowlist = allowlistService.addAllowlist(allowlist);
        JobDetail jobDetail = schedulerService.findJob("AllowlistCheckJob", "DEFAULT");
        scheduler.triggerJob(jobDetail.getKey());
        allowlistService.checkAllowlists();
        allowlist = allowlistService.findAllowList(allowlist.getId());
        assertTrue(allowlistService.isAllowlisted(allowlist.getEntry()));
        assertTrue(allowlist.isExpired());
        schedulerService.deleteJob(jobDetail.getKey());
    }

    @Test
    public void notAllowlistedTest() {
        assertNull(allowlistRepository.findByEntry("testing"));
        assertFalse(allowlistService.isAllowlisted("testing"));
    }

    @Test
    public void getAllowlistUidsTest() {
        assertThat(allowlistService.getAllAllowlistUids().size(), greaterThanOrEqualTo(2));
    }
}
