package edu.hawaii.its.filedrop.service.mail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.type.FileDrop;

@Service
public class EmailService {

    private static final Log logger = LogFactory.getLog(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${app.mail.enabled}")
    private boolean isEnabled;

    @Value("${app.mail.from:no-reply}")
    private String from;

    @Autowired
    private ITemplateEngine htmlTemplateEngine;

    @Autowired
    private MailComponentLocator mailComponentLocator;

    @Autowired
    private LdapService ldapService;

    @Value("${url.base}")
    private String url;

    // Constructor
    public EmailService() {
        // empty
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void send(Mail mail, String template, Context context) {
        logger.info("Sending email from send(mail, template, context)");
        if (isEnabled && mail.getFrom() != null && mail.getTo() != null) {
            String htmlContent = htmlTemplateEngine.process("mail/" + template, context);
            MailTemplate mailTemplate = mailComponentLocator.find(template);
            MimeMessagePreparator msg = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setFrom(mail.getFrom());
                messageHelper.setTo(mail.getTo());
                messageHelper.setSubject(mailTemplate.getSubject());
                messageHelper.setText(htmlContent, true);
            };

            javaMailSender.send(msg);
        }
    }

    public void send(Mail mail) {
        logger.info("Sending email from send(user)...");
        if (isEnabled && mail.getTo() != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(mail.getTo());
            msg.setFrom(mail.getFrom());
            msg.setText(mail.getContent());
            msg.setSubject(mail.getSubject());
            try {
                javaMailSender.send(msg);
            } catch (MailException ex) {
                logger.error("Error", ex);
            }
        }
    }

    public Map<String, Object> getFileDropContext(String key, FileDrop fileDrop) {
        Map<String, Object> contextMap = new HashMap<>();

        contextMap.put("sender", fileDrop.getUploader());
        contextMap.put("downloadURL", url + "/dl/" + fileDrop.getDownloadKey());
        contextMap.put("expiration", fileDrop.getExpiration());

        if (key.equals("uploader")) {
            Map<String, String> recipientMap = new HashMap<>();

            fileDrop.getRecipients().forEach(recipient -> {
                LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);
                if (ldapPerson.isValid()) {
                    recipientMap.put(recipient, ldapPerson.getCn());
                } else {
                    recipientMap.put(recipient, recipient);
                }
            });

            contextMap.put("recipients", recipientMap);
        }

        return contextMap;
    }

    public String getFrom() {
        return from;
    }
}
