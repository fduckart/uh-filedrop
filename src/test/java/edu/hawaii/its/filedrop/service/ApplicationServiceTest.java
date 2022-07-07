package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Faq;
import edu.hawaii.its.filedrop.type.Office;
import edu.hawaii.its.filedrop.type.Setting;

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
        office.setSortId(9999);

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
    public void findSettings() {
        List<Setting> settings = applicationService.findSettings();
        assertThat(settings.size(), greaterThanOrEqualTo(1));
        assertThat(settings.get(0).getId(), equalTo(1));
        assertThat(settings.get(0).getKey(), equalTo("disableLanding"));
        assertThat(settings.get(0).getValue(), equalTo("false"));

        List<Setting> cache = applicationService.findSettings();

        assertSame(settings, cache);

        applicationService.evictSettingCache();

        assertNotSame(cache, applicationService.findSettings());
    }

    @Test
    public void findSettingById() {
        Setting setting = applicationService.findSetting(1);
        Setting setting1 = applicationService.findSetting(1);

        assertThat(setting.getId(), equalTo(1));
        assertThat(setting.getKey(), equalTo("disableLanding"));
        assertThat(setting.getValue(), equalTo("false"));
        assertThat(setting1.getId(), equalTo(1));
        assertThat(setting1.getKey(), equalTo("disableLanding"));
        assertThat(setting1.getValue(), equalTo("false"));

        assertSame(setting, setting1);
    }

    @Test
    public void saveSetting() {
        Setting setting = applicationService.findSetting(1);
        setting.setValue("true");
        assertThat(setting.getValue(), equalTo("true"));
        Setting setting1 = applicationService.saveSetting(setting);
        assertThat(setting1.getValue(), equalTo("true"));
        setting.setValue("false");
        setting = applicationService.saveSetting(setting);
        applicationService.evictSettingCache();
        assertThat(setting.getValue(), equalTo("false"));
    }

    @Test
    public void findFaq() {
        List<Faq> faqs = applicationService.findFaqs();
        assertThat(faqs.size(), greaterThanOrEqualTo(1));
        assertThat(faqs.get(0).getId(), equalTo(1));
        assertThat(faqs.get(0).getQuestion(), containsString("How does it work?"));
        assertThat(faqs.get(0).getAnswer(), startsWith("The basic idea behind"));

        List<Faq> cache = applicationService.findFaqs();

        assertSame(faqs, cache);

        applicationService.evictFaqCache();

        assertNotSame(cache, applicationService.findFaqs());
    }

    @Test
    public void findFaqById() {
        Faq faq = applicationService.findFaq(1);
        Faq faq1 = applicationService.findFaq(1);

        assertThat(faq.getId(), equalTo(1));
        assertThat(faq.getQuestion(), containsString("How does it work?"));
        assertThat(faq.getAnswer(), startsWith("The basic idea behind"));
        assertThat(faq1.getId(), equalTo(1));
        assertThat(faq1.getQuestion(), containsString("How does it work?"));
        assertThat(faq1.getAnswer(), startsWith("The basic idea behind"));

        assertSame(faq, faq1);
    }

    @Test
    public void saveFaq() {
        Faq faq = applicationService.findFaq(1);
        faq.setAnswer("test answer");
        assertThat(faq.getAnswer(), equalTo("test answer"));
        Faq faq1 = applicationService.saveFaq(faq);
        assertThat(faq1.getAnswer(), equalTo("test answer"));
        faq.setAnswer("another answer");
        faq = applicationService.saveFaq(faq);
        applicationService.evictSettingCache();
        assertThat(faq.getAnswer(), equalTo("another answer"));
    }
}
