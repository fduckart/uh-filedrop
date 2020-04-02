package edu.hawaii.its.filedrop.service.mail;

import org.springframework.stereotype.Component;

@Component("department")
public class DepartmentMail implements MailTemplate {

    @Override
    public String getSubject() {
        return "Report: Files available at the UH FileDrop Service";
    }

    @Override
    public String toString() {
        return "DepartmentMail [subject=" + getSubject() + "]";
    }
}
