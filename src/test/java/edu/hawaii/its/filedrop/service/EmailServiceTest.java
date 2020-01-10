package edu.hawaii.its.filedrop.service;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.context.Context;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Mail;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@ActiveProfiles(profiles = "localhost")
@RunWith(SpringRunner.class)
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Rule
    public GreenMailRule server = new GreenMailRule(new ServerSetup(1025, "localhost", "smtp"));

    @Before
    public void setUp() {
        server.start();
        emailService.setEnabled(true);
    }

    @After
    public void tearDown() {
        server.stop();
        emailService.setEnabled(false);
    }

    @Test
    public void send() throws MessagingException, IOException {
        assertTrue(emailService.isEnabled());

        Mail mail = new Mail();
        mail.setFrom(emailService.getFrom());
        mail.setTo("frank@example.com");
        mail.setSubject("Test Email");
        mail.setContent("Test");

        emailService.send(mail);

        MimeMessage[] receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
        assertThat(receivedMessages[0].getFrom()[0].toString(), equalTo("no-reply@its.hawaii.edu"));
        assertThat(receivedMessages[0].getAllRecipients()[0].toString(), equalTo("frank@example.com"));
        assertThat(receivedMessages[0].getSubject(), equalTo("Test Email"));
        assertThat(receivedMessages[0].getContent().toString(), containsString("Test"));
    }

    @Test
    public void sendNull() {
        assertTrue(emailService.isEnabled());
        Mail mail = new Mail();
        mail.setTo("test@example.com");
        emailService.send(mail);

        MimeMessage[] receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(0));

        emailService.send(new Mail());

        receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(0));
    }

    @Test
    public void sendTemplate() throws MessagingException, IOException {
        assertTrue(emailService.isEnabled());

        Mail mail = new Mail();
        mail.setTo("test@google.com");
        mail.setFrom("test2@google.com");
        mail.setSubject("Test Email");

        Context context = new Context();
        context.setVariable("sender", mail.getFrom());
        context.setVariable("size", 9999);
        context.setVariable("expiration", LocalDateTime.now());
        context.setVariable("comment", "This is a test");
        context.setVariable("downloadURL", "https://google.com");

        emailService.send(mail, "mail/receiver", context);

        MimeMessage[] receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
        assertThat(receivedMessages[0].getFrom()[0].toString(), equalTo("test2@google.com"));
        assertThat(receivedMessages[0].getAllRecipients()[0].toString(), equalTo("test@google.com"));
        assertThat(receivedMessages[0].getContent().toString(), containsString("9999 bytes"));
    }
}
