package edu.hawaii.its.filedrop.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;

@Controller
public class DownloadController {

    private static final Log logger = LogFactory.getLog(DownloadController.class);

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private UserContextService userContextService;

    @GetMapping(value = "/dl/{downloadKey}")
    public String download(Model model, @PathVariable String downloadKey) {
        FileDrop fileDrop = fileDropService.findFileDrop(downloadKey);
        if (fileDrop == null) {
            model.addAttribute("error", "Download not found");
            return "user/download-error";
        }

        if (fileDrop.isAuthenticationRequired()) {
           return "redirect:/sl/" + fileDrop.getDownloadKey();
        }

        model.addAttribute("fileDrop", fileDrop);
        return "user/download";
    }

    @GetMapping(value = "/sl/{downloadKey}")
    @PreAuthorize("isAuthenticated()")
    public String downloadSecure(Model model, @PathVariable String downloadKey) {
        FileDrop fileDrop = fileDropService.findFileDrop(downloadKey);
        String recipient = fileDrop.getRecipient().substring(1, fileDrop.getRecipient().length() - 2);
        List<String> recipientList = Arrays.asList(recipient.split(", "));
        logger.debug("Current User: " + currentUser().getUsername());
        logger.debug("Uploader: " + fileDrop.getUploader());
        if(!(recipientList.contains(currentUser().getUsername()) || fileDrop.getUploader().equals(currentUser().getUsername()))) {
            model.addAttribute("error", "You are not a recipient for this drop.");
            return "user/download-error";
        }
        model.addAttribute("fileDrop", fileDrop);
        return "user/download";
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }
}
