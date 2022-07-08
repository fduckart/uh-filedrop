package edu.hawaii.its.filedrop.controller;

import java.time.LocalDateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.WorkflowService;

@ControllerAdvice
public class ErrorControllerAdvice {

    private static final Log logger = LogFactory.getLog(ErrorControllerAdvice.class);

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UserContextService userContextService;

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        User user = currentUser();
        String username = user.getUsername();
        logger.error("username: " + username + "; Exception: " + e, e);

        model.addAttribute("error", "Very funny, wiseguy.");
        model.addAttribute("timestamp", LocalDateTime.now().toString());
        model.addAttribute("message", e.getMessage());

        workflowService.stopProcess(user);

        return "error";
    }

    public User currentUser() {
        return userContextService.getCurrentUser();
    }

}