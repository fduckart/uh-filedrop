package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileDropInfoTest {
    FileDropInfo fileDropInfo;

    @BeforeEach
    public void setup() {
        fileDropInfo = new FileDropInfo();
    }

    @Test
    public void accessors() {
        assertThat(fileDropInfo.getFileDropId(), equalTo(null));
        assertThat(fileDropInfo.getCreated(), equalTo(null));
        assertThat(fileDropInfo.getExpiration(), equalTo(null));

        fileDropInfo.setFileDropId(666);
        fileDropInfo.setCreated(LocalDateTime.of(2020, 4, 20, 4, 20, 20));
        fileDropInfo.setExpiration(LocalDateTime.of(2020, 4, 21, 4, 20, 20));
        assertThat(fileDropInfo.getFileDropId(), equalTo(666));
        assertThat(fileDropInfo.getCreated(),
                equalTo(LocalDateTime.of(2020, 04, 20, 4, 20, 20)));
        assertThat(fileDropInfo.getExpiration(),
                equalTo(LocalDateTime.of(2020, 04, 21, 4, 20, 20)));

        List<FileDropInfo.FileInfo> fileInfoList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FileDropInfo.FileInfo fileInfo = new FileDropInfo.FileInfo();
            fileInfo.setDownloads(1);
            fileInfo.setFileType("text");
            fileInfo.setFileSize(1000L);
            fileInfo.setFileName("test" + i + ".txt");
            fileInfoList.add(fileInfo);
        }
        fileDropInfo.setFileInfoList(fileInfoList);
        assertThat(fileDropInfo.getFileInfoList().size(), equalTo(5));
        assertThat(fileDropInfo.getFileInfoList().get(0).getFileName(), equalTo("test0.txt"));
        assertThat(fileDropInfo.getFileInfoList().get(0).getFileSize(), equalTo(1000L));
        assertThat(fileDropInfo.getFileInfoList().get(0).getFileType(), equalTo("text"));
        assertThat(fileDropInfo.getFileInfoList().get(0).getDownloads(), equalTo(1));
        fileDropInfo.setDownloads(fileDropInfo.getFileInfoList().stream().mapToInt(FileDropInfo.FileInfo::getDownloads).sum());
        assertThat(fileDropInfo.getDownloads(), equalTo(5));
    }
}
