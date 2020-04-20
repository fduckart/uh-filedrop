package edu.hawaii.its.filedrop.controller;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.repository.ValidationRepository;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.Validation;
import edu.hawaii.its.filedrop.util.Strings;

@Controller
public class ValidationController {

    private Log logger = LogFactory.getLog(ValidationController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private ValidationRepository validationRepository;

    @PostMapping("/validate")
    public String validation(HttpServletRequest request, Model model, @RequestParam("name") String name, @RequestParam("email") String email) {
        Validation validation = new Validation();
        validation.setValidationKey(Strings.generateRandomString());
        validation.setName(name);
        validation.setAddress(email);
        validation.setCreated(LocalDateTime.now());
        validation.setIpAddress(request.getRemoteAddr());

        validation = validationRepository.save(validation);

        Mail mail = new Mail();
        mail.setTo(email);
        mail.setFrom(emailService.getFrom());

        Context context = new Context();
        context.setVariable("email", email);

        emailService.send(mail, "validation", context);

        model.addAttribute("email", email);

        logger.debug("validation; " + validation);

        return "validation/validation-sent";
    }
}
