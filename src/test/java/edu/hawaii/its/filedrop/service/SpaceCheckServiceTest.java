package edu.hawaii.its.filedrop.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpaceCheckServiceTest {

    @Autowired
    private SpaceCheckService spaceCheckService;

    @Test
    public void construction() {
        assertNotNull(spaceCheckService);
    }

    @Test
    public void freeSpace() {
        spaceCheckService.update();
        assertTrue(spaceCheckService.isFreeSpaceAvailable());

        spaceCheckService.setBytesFree(0);
        assertFalse(spaceCheckService.isFreeSpaceAvailable());
    }

    @Test
    public void usedSpace() {
        spaceCheckService.update();

        assertEquals(spaceCheckService.getBytesUsed(), 0);

        spaceCheckService.setBytesUsed(1000);
        assertEquals(spaceCheckService.getBytesUsed(), 1000);
    }
}
