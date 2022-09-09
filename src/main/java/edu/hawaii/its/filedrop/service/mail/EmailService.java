package edu.hawaii.its.filedrop.service.mail;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.type.FileDrop;

@Service
public class EmailService {

    private static final Log logger = LogFactory.getLog(EmailService.class);

    @Value("${app.mail.enabled}")
    private boolean isEnabled;

    @Value("${app.mail.from:no-reply}")
    private String from;

    @Value("${spring.mail.port:-1}")
    private int port;

    @Value("${url.base}")
    private String url;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private ITemplateEngine htmlTemplateEngine;

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private MailComponentLocator mailComponentLocator;

    // Constructor.
    public EmailService() {
        // Empty
    }

    @PostConstruct
    public void init() {
        logger.info("init; starting");
        logger.info("init; enabled? " + isEnabled);
        javaMailSender.setPort(port);
        logger.info("init; port: " + javaMailSender.getPort());
        logger.info("init; finished");
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void send(Mail mail, String template, Map<String, Object> map) {
        send(mail, template, new Context(Locale.ENGLISH, map));
    }

    public void send(Mail mail, String template, Context context) {
        if (isEnabled() && mail.getFrom() != null && mail.getTo() != null) {
            String htmlContent = htmlTemplateEngine.process("mail/" + template, context);
            MailTemplate mailTemplate = mailComponentLocator.find(template);
            MimeMessagePreparator msg = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setFrom(mail.getFrom());
                messageHelper.setTo(mail.getTo());
                messageHelper.setTo("duckart@hawaii.edu"); /// UN-DO THIS
                if (mail.getBcc() != null && mail.getBcc().length() > 0) {
                    messageHelper.setBcc(mail.getBcc());
                }
                messageHelper.setSubject(mailTemplate.getSubject());
                messageHelper.setText(htmlContent, true);
            };

            logger.debug("Send email: " + mail);
            javaMailSender.send(msg);
        }
    }

    public void send(Mail mail) {
        logger.info("Sending email from send(user)...");
        if (isEnabled() && mail.getTo() != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(mail.getTo());
            msg.setTo("duckart@hawaii.edu"); /// UN-DO
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

            fileDropService.findRecipients(fileDrop).forEach(recipient -> {
                LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient.getName());
                if (ldapPerson.isValid()) {
                    recipientMap.put(recipient.getName(), ldapPerson.getCn());
                } else {
                    recipientMap.put(recipient.getName(), recipient.getName());
                }
            });

            contextMap.put("recipients", recipientMap);
        }

        return contextMap;
    }

    public Context fileDropContext(String key, FileDrop fileDrop) {
        return new Context(Locale.ENGLISH, getFileDropContext(key, fileDrop));
    }

    public String getFrom() {
        return from;
    }

    public JavaMailSenderImpl getJavaMailSender() {
        return javaMailSender;
    }
}
