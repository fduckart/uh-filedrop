package edu.hawaii.its.filedrop.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Setting;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@TestMethodOrder(MethodOrderer.Random.class)
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
