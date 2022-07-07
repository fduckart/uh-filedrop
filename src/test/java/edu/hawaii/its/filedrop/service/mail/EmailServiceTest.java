package edu.hawaii.its.filedrop.service.mail;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class EmailServiceTest {

    @RegisterExtension
    static GreenMailExtension server = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileDropService fileDropService;

    @BeforeEach
    public void setUp() {
        emailService.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        emailService.setEnabled(false);
    }

    @Test
    public void send() throws MessagingException, IOException {
        assertTrue(emailService.isEnabled());

        Mail mail = new Mail();
        mail.setFrom(emailService.getFrom());
        mail.setTo("frank@example.com");
        mail.setBcc("duckart@acm.org");
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
    public void sendTemplate() throws Exception {
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

        emailService.send(mail, "receiver", context);

        MimeMessage[] receivedMessages = server.getReceivedMessages();

        assertThat(receivedMessages.length, equalTo(1));
        assertThat(receivedMessages[0].getFrom()[0].toString(), equalTo("test2@google.com"));
        assertThat(receivedMessages[0].getAllRecipients()[0].toString(), equalTo("test@google.com"));
        assertThat(receivedMessages[0].getContent().toString(), containsString("9999 bytes"));
        assertThat(receivedMessages[0].getSubject(), startsWith("Files are available ")); // Note.

        final GreenMailUser user = server.setUser("duckart@acm.org", "");
        MailFolder inbox = server.getManagers().getImapHostManager().getInbox(user);
        List<StoredMessage> messages = inbox.getMessages();
        assertThat(messages.size(), equalTo(0));

        mail = new Mail();
        mail.setTo("duckart@computer.org");
        mail.setBcc("duckart@acm.org");
        mail.setFrom("no-reply@hawaii.edu");
        mail.setSubject("Test Email Two");

        emailService.send(mail, "receiver", context);

        receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(3)); // Why did it go up 2?

        assertThat(receivedMessages[1].getFrom().length, equalTo(1));
        assertThat(receivedMessages[1].getFrom()[0].toString(), equalTo(mail.getFrom()));
        assertThat(receivedMessages[1].getAllRecipients().length, equalTo(1));
        assertThat(receivedMessages[1].getAllRecipients()[0].toString(), equalTo(mail.getTo()));

        assertThat(receivedMessages[2].getFrom().length, equalTo(1));
        assertThat(receivedMessages[2].getFrom()[0].toString(), equalTo(mail.getFrom()));
        assertThat(receivedMessages[2].getAllRecipients().length, equalTo(1));
        assertThat(receivedMessages[2].getAllRecipients()[0].toString(), equalTo(mail.getTo()));

        inbox = server.getManagers().getImapHostManager().getInbox(user);
        messages = inbox.getMessages();
        assertThat(messages.size(), equalTo(1));
    }

    @Test
    public void sendTemplateFileDrop() throws MessagingException, IOException {
        assertTrue(emailService.isEnabled());

        Mail mail = new Mail();
        mail.setFrom("jwlennon@hawaii.edu");
        mail.setTo("krichards@example.com");
        mail.setSubject("Test Email");

        FileDrop fileDrop = fileDropService.findFileDrop(3);

        Context context = emailService.fileDropContext("receiver", fileDrop);

        emailService.send(mail, "receiver", context);

        MimeMessage[] receivedMessages = server.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
        assertThat(receivedMessages[0].getAllRecipients()[0].toString(), equalTo("krichards@example.com"));
        assertThat(receivedMessages[0].getFrom()[0].toString(), equalTo("jwlennon@hawaii.edu"));
        assertThat(receivedMessages[0].getContent().toString(), containsString("jwlennon@hawaii.edu"));

        context = new Context(Locale.ENGLISH, emailService.getFileDropContext("uploader", fileDrop));
        @SuppressWarnings("unchecked")
        Map<String, Object> recipients = (Map<String, Object>) context.getVariable("recipients");
        assertThat(recipients.size(), equalTo(1));
        assertThat(recipients.get("krichards@example.com"), equalTo("Keith Richards"));
    }

    @Test
    public void misc() {
        FileDrop fileDrop = fileDropService.findFileDrop(3);
        Context context = emailService.fileDropContext("recipients", fileDrop);

        @SuppressWarnings("unchecked")
        Map<String, String> recipients = (Map<String, String>) context.getVariable("recipients");
        assertThat(recipients, equalTo(null));
    }
}
