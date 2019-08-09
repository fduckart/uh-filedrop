package edu.hawaii.its.filedrop.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapService;

@Controller
public class PrepareController {

    private Log logger = LogFactory.getLog(PrepareController.class);

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private LdapService ldapService;


    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipient(Model model, @RequestParam("recipient") String recipient) {
        logger.debug("User added recipient.");
        fileDropService.addRecipient(userContextService.getCurrentUser(), recipient);
//        LdapPerson ldapPerson = ldapService.findByUid(recipient);
        return "redirect:/prepare/files";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files")
    public String addFiles(Model model) {
        logger.debug("User at addFiles.");
//        logger.debug(recipient);
//        model.addAttribute("recipient", recipient);
        return "user/files";
    }
}
