package edu.hawaii.its.filedrop.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Setting;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SettingRepositoryTest {

    @Autowired
    private SettingRepository settingRepository;

    @Test
    public void findSettings() {
        Setting setting = settingRepository.findById(1).get();
        assertThat(setting.getKey(), equalTo("disableLanding"));
        assertThat(setting.getValue(), equalTo("false"));
        assertThat(setting.getId(), equalTo(1));

        setting.setValue("true");
        settingRepository.save(setting);

        assertThat(setting.getValue(), equalTo("true"));

        setting.setValue("false");
        settingRepository.save(setting);
    }

}
