package edu.hawaii.its.filedrop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.type.FileDrop;

@Controller
public class DownloadController {

    @Autowired
    private FileDropService fileDropService;

    @GetMapping(value = "/dl/{downloadKey}")
    @PreAuthorize("isAuthenticated()")
    public String getDownload(Model model, @PathVariable String downloadKey) {
        FileDrop fileDrop = fileDropService.findFileDrop(downloadKey);
        if (fileDrop == null) {
            throw new NullPointerException(downloadKey + " is not valid.");
        }
        model.addAttribute("fileDrop", fileDrop);
        return "user/download";
    }
}
