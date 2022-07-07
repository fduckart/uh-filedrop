package edu.hawaii.its.filedrop.service.mail;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MailTemplateTest {

    @Autowired
    private MailComponentLocator mailComponentLocator;

    @Test
    public void construct() {
        assertNotNull(mailComponentLocator);
        assertThat(mailComponentLocator.getMails().size(), equalTo(6));
    }

    @Test
    public void find() {
        MailTemplate mailTemplate = mailComponentLocator.find("receiver");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("Files are available for you at the UH FileDrop Service"));

        mailTemplate = mailComponentLocator.find("uploader");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("Your files have been received by the UH FileDrop Service"));

        mailTemplate = mailComponentLocator.find("allowlist");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("FileDrop Allowlist"));

        mailTemplate = mailComponentLocator.find("validation");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("FileDrop email validation"));

        mailTemplate = mailComponentLocator.find("empty");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("Empty Email"));

        mailTemplate = mailComponentLocator.find("test");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("Empty Email"));
    }
}
