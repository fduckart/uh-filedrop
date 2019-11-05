package edu.hawaii.its.filedrop.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.crypto.Ciphers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CipherServiceTest {

    @Autowired
    private CipherService cipherService;

    @Autowired
    private Ciphers ciphers;

    @Test
    public void construct() {
        assertNotNull(ciphers);
        assertNotNull(cipherService);
    }

    @Test
    public void testEncryption() throws IOException {
        File original = File.createTempFile("~filedrop.store.original", "txt");
        original.deleteOnExit();
        assertTrue(original.exists());

        File encrypted = File.createTempFile("~filedrop.store.encrypted", "txt");
        encrypted.deleteOnExit();
        assertThat(encrypted.length(), equalTo(0L));

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(original));
        StringBuilder content = new StringBuilder();

        content.append("I'd like to be");
        content.append("under the sea");
        content.append("in an octopus's garden");
        content.append("in the shade");
        content.append("- Ringo");

        bufferedWriter.write(content.toString());
        bufferedWriter.close();

        assertTrue(Files.exists(original.toPath()));
        assertThat(original.length(), greaterThan(0L));
        assertThat(Files.size(original.toPath()), equalTo(original.length()));

        cipherService.encryptFile("test", original, encrypted);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(encrypted));
        StringBuilder builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), not(equalTo(content.toString())));
    }

    @Test
    public void testDecryption() throws IOException {
        File original = File.createTempFile("~filedrop.store.original", "txt");
        original.deleteOnExit();
        assertTrue(original.exists());

        File encrypted = File.createTempFile("~filedrop.store.encrypted", "txt");
        assertThat(encrypted.length(), equalTo(0L));

        File decrypted = File.createTempFile("~filedrop.store.decrypted", "txt");
        assertThat(encrypted.length(), equalTo(0L));

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(original));
        StringBuilder content = new StringBuilder();

        content.append("I'd like to be");
        content.append("under the sea");
        content.append("in an octopus's garden");
        content.append("in the shade");
        content.append("- Ringo");

        bufferedWriter.write(content.toString());
        bufferedWriter.close();

        assertTrue(Files.exists(original.toPath()));
        assertThat(original.length(), greaterThan(0L));
        assertThat(Files.size(original.toPath()), equalTo(original.length()));

        cipherService.encryptFile("test", original, encrypted);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(encrypted));
        StringBuilder builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), not(equalTo(content.toString())));

        cipherService.decryptFile("test", encrypted, decrypted);

        bufferedReader = new BufferedReader(new FileReader(decrypted));
        builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), equalTo(content.toString()));
    }

}
