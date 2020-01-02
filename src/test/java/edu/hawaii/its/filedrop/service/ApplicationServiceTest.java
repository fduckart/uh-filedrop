package edu.hawaii.its.filedrop.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Office;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Test
    public void findOffices() {
        List<Office> offices = applicationService.findOffices();

        Office o0 = offices.get(0);
        assertThat(o0.getId(), equalTo(1));
        assertThat(o0.getCampusId(), equalTo(1));
        assertThat(o0.getDescription(), containsString("Office"));

        List<Office> list = applicationService.findOffices();
        assertSame(list, offices);// Check if caching is working.

        applicationService.evictOfficeCaches();
        assertNotSame(list, applicationService.findOffices());
    }

    @Test
    public void findOfficeById() {
        Office s0 = applicationService.findOffice(1);
        Office s1 = applicationService.findOffice(1);

        assertThat(s0.getId(), equalTo(1));
        assertThat(s1.getId(), equalTo(1));
        assertEquals(s0, s1);
        assertSame(s0, s1);// Check if caching is working.
    }

    @Test
    public void addOffice() {
        List<Office> offices = applicationService.findOffices();
        final int count0 = offices.size();
        Office oY = offices.get(offices.size() - 1);

        // Make sure state id doesn't exist first.
        Integer id = oY.getId() + 1;
        Office office = applicationService.findOffice(id);
        assertNull(office);

        office = new Office();
        office.setCampusId(7);
        office.setDescription("New Manoa Campus Office");

        // What we are testing.
        applicationService.addOffice(office);

        // Check that we have a new record.
        int count1 = applicationService.findOffices().size();
        assertThat(count1, equalTo(count0 + 1));

        // Check the new record.
        Office oZ = applicationService.findOffice(id);
        assertThat(oZ, equalTo(office));

        // Ensure we didn't upset the caching.
        Office c0 = applicationService.findOffice(id);
        assertSame(oZ, office);
        assertSame(oZ, c0);
    }
}
