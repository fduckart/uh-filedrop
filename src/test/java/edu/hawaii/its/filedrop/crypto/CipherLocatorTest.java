package edu.hawaii.its.filedrop.crypto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

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
}
