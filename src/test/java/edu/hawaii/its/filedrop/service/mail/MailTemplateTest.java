package edu.hawaii.its.filedrop.service.mail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MailTemplateTest {

    @Autowired
    private MailComponentLocator mailComponentLocator;

    @Test
    public void construct() {
        assertNotNull(mailComponentLocator);
        assertThat(mailComponentLocator.getMails().size(), equalTo(3));
    }

    @Test
    public void find() {
        MailTemplate mailTemplate = mailComponentLocator.find("receiver");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("Files are available for you at the UH FileDrop Service"));

        mailTemplate = mailComponentLocator.find("uploader");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("Your files have been received by the UH FileDrop Service"));

        mailTemplate = mailComponentLocator.find("whitelist");
        assertNotNull(mailTemplate);
        assertThat(mailTemplate.getSubject(), equalTo("FileDrop Whitelist"));
    }
}
