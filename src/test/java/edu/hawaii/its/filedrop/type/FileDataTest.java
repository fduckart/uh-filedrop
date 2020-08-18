package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class FileDataTest {

    private FileData fileData;

    @Before
    public void setUp() {
        fileData = new FileData();
    }

    @Test
    public void accessors() {
        assertNull(fileData.getId());
        assertNull(fileData.getFileName());
        assertNotNull(fileData.getComment());

        fileData.setId(666);
        assertThat(fileData.getId(), equalTo(666));

        fileData.setComment(null);
        assertThat(fileData.getComment(), equalTo(""));

        fileData.setComment("");
        assertThat(fileData.getComment(), equalTo(""));

        fileData.setComment("top");
        assertThat(fileData.getComment(), equalTo("top"));
    }

    @Test
    public void testToString() {
        System.out.println("aaa");
        System.out.println(">>>>>>>>>>>>>>>>> fileData: " + fileData);
        System.out.println("bbb");

        assertThat(fileData.toString(), containsString("FileData [id=null,"));
        fileData.setId(1);
        assertThat(fileData.toString(), containsString("FileData [id=1,"));
        fileData.setId(12);
        assertThat(fileData.toString(), containsString("FileData [id=12,"));
        fileData.setId(123);
        assertThat(fileData.toString(), containsString("FileData [id=123,"));
        fileData.setId(1234);
        assertThat(fileData.toString(), containsString("FileData [id=1234,"));
    }
}
