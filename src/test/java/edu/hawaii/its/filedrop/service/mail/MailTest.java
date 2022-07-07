package edu.hawaii.its.filedrop.service.mail;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MailTest {

    private Mail mail;

    @BeforeEach
    public void setUp() {
        mail = new Mail();
    }

    @Test
    public void construct() {
        assertNotNull(mail);

        mail = new Mail("test@example.com", "test2@example.com", "Test subject", "Test content");

        assertThat(mail.getFrom(), equalTo("test@example.com"));
        assertThat(mail.getTo(), equalTo("test2@example.com"));
        assertThat(mail.getSubject(), equalTo("Test subject"));
        assertThat(mail.getContent(), equalTo("Test content"));
    }

    @Test
    public void accessors() {
        assertNull(mail.getFrom());
        assertNull(mail.getTo());
        assertNull(mail.getContent());
        assertNull(mail.getSubject());

        mail.setFrom("test@example.com");
        assertThat(mail.getFrom(), equalTo("test@example.com"));
        mail.setTo("test2@example.com");
        assertThat(mail.getTo(), equalTo("test2@example.com"));
        mail.setContent("Test content");
        assertThat(mail.getContent(), equalTo("Test content"));
        mail.setSubject("Test Subject");
        assertThat(mail.getSubject(), equalTo("Test Subject"));
    }

    @Test
    public void toStringTest() {
        mail.setFrom("test@example.com");
        mail.setTo("test2@example.com");
        mail.setContent("Test content");
        mail.setSubject("Test subject");
        assertThat(mail.toString(),
                containsString(
                        "Mail [from=test@example.com, to=test2@example.com, subject=Test subject, content=Test content]"));
    }
}
