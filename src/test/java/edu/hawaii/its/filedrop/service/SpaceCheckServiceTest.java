package edu.hawaii.its.filedrop.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SpaceCheckServiceTest {

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Test
    public void construct() {
    }

    @Test
    public void maxUploadSizeTest() {
        long maxUploadSize = spaceCheckService.getMaxUploadSize();

        assertThat(spaceCheckService.getMaxUploadSize(), equalTo(999 * 1024 * 1024L));

        spaceCheckService.setMaxUploadSize(1L);
        assertThat(spaceCheckService.getMaxUploadSize(), not(equalTo(1L)));
        assertThat(spaceCheckService.getMaxUploadSize(), equalTo(1000000L));

        spaceCheckService.setMaxUploadSize(maxUploadSize);
    }

    @Test
    public void reservedSpaceTest() {
        assertTrue(spaceCheckService.isFreeSpaceAvailable());

        spaceCheckService.setReservedSpace(0L);
        assertTrue(spaceCheckService.isFreeSpaceAvailable());
        spaceCheckService.setBytesFree(0L);
        assertFalse(spaceCheckService.isFreeSpaceAvailable());

        spaceCheckService.setReservedSpace(1000000000);
        spaceCheckService.update();
    }

    @Test
    public void bytesFreeTest() {
        assertThat(spaceCheckService.getBytesFree(), greaterThan(0L));

        spaceCheckService.setBytesFree(0L);
        assertThat(spaceCheckService.getBytesFree(), equalTo(0L));

        spaceCheckService.update();
        assertThat(spaceCheckService.getBytesFree(), greaterThan(0L));
    }

    @Test
    public void bytesUsedTest() {
        assertThat(spaceCheckService.getBytesUsed(), greaterThan(0L));

        spaceCheckService.setBytesUsed(0L);
        assertThat(spaceCheckService.getBytesUsed(), equalTo(0L));

        spaceCheckService.update();
        assertThat(spaceCheckService.getBytesUsed(), greaterThan(0L));
    }
}
