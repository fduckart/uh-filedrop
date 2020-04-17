package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("validation")
public class ValidationMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "FileDrop email validation";
    }

    @Override
    public String toString() {
        return "ValidationMail [subject=" + getSubject() + "]";
    }
}
