package edu.hawaii.its.filedrop.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.Message;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@TestMethodOrder(MethodOrderer.Random.class)
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void findMessages() {
        Message m1 = messageRepository.findById(2).get();
        Message m2 = messageRepository.findById(2).get();
        assertThat(m1, equalTo(m2));
        assertThat(m1.getText(), startsWith("Welcome to the University of"));

        m2 = m1;
        assertSame(m1, m2);

        m1.setText("Test");
        messageRepository.save(m1);

        assertThat(m1.getText(), equalTo("Test"));
    }

}
