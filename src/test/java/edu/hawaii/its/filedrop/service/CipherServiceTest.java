package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.crypto.Ciphers;
import edu.hawaii.its.filedrop.type.FileDrop;

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
    public void testEncrypt() throws Exception {
        String originalContent = "foobar";
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
        String cipher = "AES/CBC/PKCS5Padding";
        FileEncrypterDecrypter fileEncrypterDecrypter = new FileEncrypterDecrypter(secretKey, cipher);
        fileEncrypterDecrypter.encrypt(originalContent, "baz.enc");
        String decryptedContent = fileEncrypterDecrypter.decrypt("baz.enc");
        assertThat(decryptedContent, is(originalContent));
        new File("baz.enc").delete(); // cleanup
    }

    @Test
    public void testNewCrypto() throws IOException, GeneralSecurityException {
        File original = File.createTempFile("~filedrop.store.original", "txt");
        original.deleteOnExit();
        assertTrue(original.exists());

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

        FileDrop fileDrop = new FileDrop();
        fileDrop.setEncryptionKey("aes:test");

        File encrypted = cipherService.encrypt(original, fileDrop);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(encrypted));
        StringBuilder builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), not(equalTo(content.toString())));

        File decrypted = cipherService.decrypt(encrypted, fileDrop);

        bufferedReader = new BufferedReader(new FileReader(decrypted));
        builder = new StringBuilder();

        bufferedReader.lines().forEach(builder::append);
        bufferedReader.close();

        assertThat(builder.toString(), equalTo(content.toString()));
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
