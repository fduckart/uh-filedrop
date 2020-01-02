package edu.hawaii.its.filedrop.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilesTest {

    @Test
    public void byteCountDisplayTest() {
        long bytes = 1024;
        assertThat(Files.byteCountToDisplaySize(bytes), equalTo("1 KB"));

        bytes *= 1024;
        assertThat(Files.byteCountToDisplaySize(bytes), equalTo("1 MB"));
    }
}
