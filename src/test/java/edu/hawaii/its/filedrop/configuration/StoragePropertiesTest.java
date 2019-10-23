package edu.hawaii.its.filedrop.configuration;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class StoragePropertiesTest {

    @Test
    public void accessors() {
        StorageProperties properties = new StorageProperties();
        assertThat(properties.getLocation(), equalTo("storage"));

        properties.setLocation("files");
        assertThat(properties.getLocation(), equalTo("files"));
    }
}
