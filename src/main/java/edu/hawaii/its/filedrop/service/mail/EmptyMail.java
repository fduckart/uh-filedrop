package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("empty")
public class EmptyMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "Empty Email";
    }

    @Override
    public String toString() {
        return "EmptyMail [subject=" + getSubject() + "]";
    }
}
