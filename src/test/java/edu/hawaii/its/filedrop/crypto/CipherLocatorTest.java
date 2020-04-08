package edu.hawaii.its.filedrop.crypto;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CipherLocatorTest {

    @Autowired
    private CipherLocator cipherLocator;

    @Test
    public void findCipher() {
        Cipher cipher;
        cipher = cipherLocator.find("rc2");
        assertThat(cipher, instanceOf(Rc2.class));

        cipher = cipherLocator.find("aes256");
        assertThat(cipher, instanceOf(Aes256.class));

        cipher = cipherLocator.find("3des");
        assertThat(cipher, instanceOf(Aes256.class));
    }

    public void encryptionProcessTest() throws IOException {
        //        Cipher cipher = cipherLocator.find("rc2");
        //        CipherFilter cipherFilter = new CipherFilter("test");
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

        //        cipher.encrypt("test", original, encrypted);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(encrypted));
        StringBuilder builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), not(equalTo(content.toString())));

        //        cipherService.decryptFile("test", encrypted, decrypted);

        bufferedReader = new BufferedReader(new FileReader(decrypted));
        builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), equalTo(content.toString()));
    }
}
