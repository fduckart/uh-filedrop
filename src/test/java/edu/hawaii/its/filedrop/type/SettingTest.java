package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SettingTest {

    Setting setting;

    @BeforeEach
    public void setUp() {
        setting = new Setting();
    }

    @Test
    public void accessors() {
        assertThat(setting.getId(), equalTo(null));
        assertThat(setting.getKey(), equalTo(null));
        assertThat(setting.getValue(), equalTo(null));

        setting.setId(999);
        setting.setKey("test");
        setting.setValue("false");
        assertThat(setting.getId(), equalTo(999));
        assertThat(setting.getKey(), equalTo("test"));
        assertThat(setting.getValue(), equalTo("false"));
    }

    @Test
    public void testToString() {
        assertThat(setting.toString(), startsWith("Setting ["));
        assertThat(setting.toString(), containsString("id=null, "));
        assertThat(setting.toString(), containsString(", key=null"));
        assertThat(setting.toString(), containsString(", value=null"));

        setting.setId(999);
        assertThat(setting.toString(), startsWith("Setting [id=999"));

        setting.setKey("test2");
        assertThat(setting.toString(), containsString(", key=test2"));

        setting.setValue("true");
        assertThat(setting.toString(), containsString(", value=true"));
    }
}
