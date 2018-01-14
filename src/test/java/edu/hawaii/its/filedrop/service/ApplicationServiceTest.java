package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.AdministratorRepository;
import edu.hawaii.its.filedrop.type.Administrator;
import edu.hawaii.its.filedrop.type.Office;
import edu.hawaii.its.filedrop.type.Role;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ApplicationServiceTest {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PersonService personService;

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

    @Test
    public void findAdministrators() {
        List<Administrator> admins = applicationService.findAdministrators();
        assertNotNull(admins);
        assertFalse(admins.isEmpty());
    }

    @Test
    public void isAdministrator() {
        List<Administrator> administratorsS = applicationService.findAdministrators();
        long administratorCount = administratorsS.size();
        assertThat(administratorCount, equalTo(9L));
        for (Administrator c : administratorsS) {
            String uhUuid = c.getPerson().getUhUuid();
            assertTrue(applicationService.isAdministrator(uhUuid));
            assertEquals(c.getRoleId(), c.getRole().getId());
            assertThat(c.getRoleId(), anyOf(
                    equalTo(Role.ID.ADMINISTRATOR.value()),
                    equalTo(Role.ID.SUPERUSER.value())));
        }

        List<Administrator> administratorsR = administratorRepository.findAll();
        for (Administrator c : administratorsR) {
            String uhUuid = c.getPerson().getUhUuid();
            assertTrue(applicationService.isAdministrator(uhUuid));
            assertEquals(c.getRoleId(), c.getRole().getId());
            assertThat(c.getRoleId(), anyOf(
                    equalTo(Role.ID.ADMINISTRATOR.value()),
                    equalTo(Role.ID.SUPERUSER.value())));
        }

        long repoSize = administratorRepository.findAll().size();
        assertThat(repoSize, equalTo(administratorCount));

        for (int i = 0; i < repoSize; i++) {
            Administrator cS = administratorsS.get(i);
            Administrator cR = administratorsR.get(i);
            assertThat(cS, equalTo(cR));
        }

        List<Administrator> administratorsX = personService.findAdministrators();
        long repoSizeTwo = administratorsX.size();
        assertThat(repoSize, equalTo(repoSizeTwo));

        for (int i = 0; i < repoSize; i++) {
            Administrator cS = administratorsS.get(i);
            Administrator cX = administratorsX.get(i);
            assertThat(cS, equalTo(cX));
        }

        List<Integer> roleIds = Arrays.asList(13, 14);
        List<Administrator> administratorsY =
                administratorRepository.findByRoleIdInOrderByOfficeIdAscIdAsc(roleIds);
        long repoSizeThree = administratorsY.size();
        assertThat(repoSize, equalTo(repoSizeThree));

        for (int i = 0; i < repoSize; i++) {
            Administrator cS = administratorsS.get(i);
            Administrator cY = administratorsY.get(i);
            assertThat(cS, equalTo(cY));
        }
    }

}
