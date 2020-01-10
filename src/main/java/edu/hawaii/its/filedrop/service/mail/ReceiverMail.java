package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("receiver")
public class ReceiverMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "Files are available for you at the UH FileDrop Service";
    }
}
