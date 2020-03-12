package edu.hawaii.its.filedrop.controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.FileSystemStorageService;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

@Controller
public class DownloadController {

    private static final Log logger = LogFactory.getLog(DownloadController.class);

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private UserContextService userContextService;

    @GetMapping(value = "/dl/{downloadKey}")
    public String download(Model model, @PathVariable String downloadKey) {
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);
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
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);
        logger.debug("downloadSecure; fileDrop: " + fileDrop + " User: " + currentUser().getUsername());

        if (!fileDropService.isAuthorized(fileDrop, currentUser().getUsername())) {
            model.addAttribute("error", "You are not a recipient for this drop.");
            return "user/download-error";
        }

        model.addAttribute("fileDrop", fileDrop);
        return "user/download";
    }

    @GetMapping(value = "/dl/{downloadKey}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String downloadKey, @PathVariable String fileName)
            throws IOException {
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);

        if (fileDrop == null) {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND)
                    .header(HttpHeaders.LOCATION, "/").build();
        }

        if ((!fileDrop.isAuthenticationRequired()) || (fileDrop.isAuthenticationRequired()
                && fileDropService.isAuthorized(fileDrop, currentUser().getUsername()))) {
            Optional<FileSet> foundFileSet =
                    fileDrop.getFileSet().stream().filter(fileSet -> fileSet.getFileName().equals(fileName))
                            .findFirst();

            if (foundFileSet.isPresent()) {
                Resource resource = storageService.loadAsResource(
                        Paths.get(fileDrop.getDownloadKey(), foundFileSet.get().getId().toString()).toString());

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.SC_NOT_FOUND)
                        .header(HttpHeaders.LOCATION, "/dl/" + downloadKey).build();
            }
        }

        return ResponseEntity.status(HttpStatus.SC_FORBIDDEN)
                .header(HttpHeaders.LOCATION, "/dl/" + downloadKey).build();
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }
}
