package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DownloadTest {

    private Download download;

    @BeforeEach
    public void setUp() {
        download = new Download();
    }

    @Test
    public void construction() {
        assertNotNull(download);
    }

    @Test
    public void accessors() {
        assertNull(download.getId());
        assertNull(download.getCompleted());
        assertNull(download.getStarted());
        assertNull(download.getFileDrop());
        assertNull(download.getFileName());
        assertNull(download.getIpAddress());
        assertNull(download.getStatus());

        download.setId(999);
        assertThat(download.getId(), equalTo(999));

        download.setCompleted(LocalDateTime.of(2020, 04, 20, 20, 20, 20));
        assertThat(download.getCompleted(), equalTo(LocalDateTime.of(2020, 04, 20, 20, 20, 20)));

        download.setStarted(LocalDateTime.of(2020, 04, 20, 20, 20, 20));
        assertThat(download.getStarted(), equalTo(LocalDateTime.of(2020, 04, 20, 20, 20, 20)));

        download.setIpAddress("0.0.0.0");
        assertThat(download.getIpAddress(), equalTo("0.0.0.0"));

        download.setStatus("test");
        assertThat(download.getStatus(), equalTo("test"));

        FileDrop fileDrop = new FileDrop();
        download.setFileDrop(fileDrop);
        assertNotNull(download.getFileDrop());
    }

    @Test
    public void testToString() {
        assertThat(download.toString(), containsString("Download [id=null, fileDrop=null, fileName=null, ipAddress=null, started=null, status=null, completed=null]"));

        download.setStatus("test");
        download.setIpAddress("0.0.0.0");
        download.setId(999);
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(999);
        download.setFileDrop(fileDrop);
        assertThat(download.toString(), containsString("Download [id=999, fileDrop=999, fileName=null, ipAddress=0.0.0.0, started=null, status=test, completed=null]"));
    }
}
