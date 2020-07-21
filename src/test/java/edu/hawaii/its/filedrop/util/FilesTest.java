package edu.hawaii.its.filedrop.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class FilesTest {

    @Test
    public void byteCountDisplayTest() {
        long bytes = 1024;
        assertThat(Files.byteCountToDisplaySize(bytes), equalTo("1 KB"));

        bytes *= 1024;
        assertThat(Files.byteCountToDisplaySize(bytes), equalTo("1 MB"));
    }

    @Test
    public void isFile() throws Exception {
        File file = null;

        assertThat(Files.isFile(null), equalTo(false));
        assertThat(Files.isFile(""), equalTo(false));

        // Create a temp file and mark 'delete on exit'.
        file = File.createTempFile("~tmp", ".tmp");
        file.deleteOnExit();

        // Put some data in temp file.
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        StringBuilder text = new StringBuilder();

        text.append("<?xml version='1.0' encoding='UTF-8' ?>\n");
        text.append("<Process>       \n");
        text.append("  <Body>        \n");
        text.append("    <DocumentTypes> \n");
        text.append("      <DocumentType>f2.pdf</DocumentType>\n");
        text.append("      <DocumentType>p2.pdf</DocumentType>\n");
        text.append("      <DocumentType>s2.pdf</DocumentType>\n");
        text.append("      <DocumentType>x2.pdf</DocumentType>\n");
        text.append("      <DocumentType>p4.pdf</DocumentType>\n");
        text.append("    </DocumentTypes>\n");
        text.append("  </Body>\n");
        text.append("</Process>    \n");

        bw.write(text.toString());
        bw.close();

        assertTrue(Files.isFile(file.getAbsolutePath()));
        assertTrue(Files.exists(file.getAbsolutePath()));
        assertFalse(Files.isDirectory(file.getAbsolutePath()));
        assertTrue(Files.isDirectory(file.getParent()));

        String path = file.getAbsolutePath();
        assertTrue(file.length() > 0L);
        assertThat(Files.fileSize(path), equalTo(file.length()));

        // Check the file contents.
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String currentLine = reader.readLine();
        while (currentLine != null) {
            builder.append(currentLine);
            builder.append("\n");
            currentLine = reader.readLine();
        }
        reader.close();
        assertThat(builder.toString(), equalTo(text.toString()));

        path = "no-file-no-way";
        assertFalse(Files.isFile(path));
        assertThat(Files.fileSize(path), equalTo(0L));
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<Files> constructor = Files.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
