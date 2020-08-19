package edu.hawaii.its.filedrop.controller;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.repository.DownloadRepository;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.FileSystemStorageService;
import edu.hawaii.its.filedrop.type.Download;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Role;

@Controller
public class DownloadController {

    private static final Log logger = LogFactory.getLog(DownloadController.class);

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private DownloadRepository downloadRepository;

    @GetMapping(value = "/expire/{downloadKey}")
    public String expire(Model model, RedirectAttributes redirectAttributes, @PathVariable String downloadKey) {
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);

        if (fileDrop == null) {
            model.addAttribute("error", "Download not found");
            return "user/download-error";
        }

        if (!currentUser().getUsername().equals(fileDrop.getUploader())) {
            model.addAttribute("error", "Download not found");
            return "user/download-error";
        }

        fileDropService.expire(fileDrop);

        logger.debug("expire; " + currentUser().getUsername() + " expired: " + fileDrop);

        redirectAttributes.addFlashAttribute("message", "Drop expired");
        return "redirect:/";
    }

    @GetMapping(value = "/dl/{downloadKey}")
    public String download(Model model, @PathVariable String downloadKey) {
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);

        if (fileDrop == null) {
            model.addAttribute("error", "Download not found");
            return "user/download-error";
        }

        if (!fileDrop.isValid()) {
            model.addAttribute("expiration", fileDrop.getExpiration());
            return "user/expired";
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

        if (fileDrop == null) {
            model.addAttribute("error", "Download not found");
            return "user/download-error";
        }

        if (!fileDrop.isValid()) {
            model.addAttribute("expiration", fileDrop.getExpiration());
            return "user/expired";
        }

        if (!fileDropService.isAuthorized(fileDrop, currentUser().getUsername())) {
            model.addAttribute("error", "Download not found");
            return "user/download-error";
        }

        model.addAttribute("fileDrop", fileDrop);
        return "user/download";
    }

    //TODO: Add decryption
    @GetMapping(value = "/dl/{downloadKey}/zip")
    public ResponseEntity<Resource> downloadFilesZip(@PathVariable String downloadKey,
                                                     HttpServletRequest request) throws IOException {
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);
        if (fileDrop == null || !fileDrop.isValid()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.LOCATION, "/")
                .build();
        }

        if (isDownloadAllowed(fileDrop)) {
            String fileName = "FileDrop(" + fileDrop.getDownloadKey() + ").zip";
            File file = new File(
                Paths.get(storageService.getRootLocation().toString(), fileDrop.getDownloadKey(), fileName).toUri());
            List<FileSet> fileSets = fileDropService.findFileSets(fileDrop);
            if (!currentUser().hasRole(Role.SecurityRole.ADMINISTRATOR)) {
                for (FileSet fileSet : fileSets) {
                    Download download = new Download();
                    download.setFileDrop(fileDrop);
                    download.setFileName(fileSet.getFileName());
                    download.setStarted(LocalDateTime.now());
                    download.setIpAddress(request.getRemoteAddr());
                    download.setStatus("INPROGRESS");
                    download.setCompleted(LocalDateTime.now());
                    downloadRepository.save(download);
                }
            }

            if (file.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                    .body(new FileUrlResource(file.getAbsolutePath()));
            } else {

                logger.debug("downloadZip; fileDrop: " + fileDrop + ", Could not download zip");

                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.LOCATION, "/dl/" + downloadKey)
                    .build();
            }
        }

        logger.debug("downloadZip; Could not find fileDrop with key: " + downloadKey);

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .header(HttpHeaders.LOCATION, "/dl/" + downloadKey)
            .build();
    }

    @GetMapping(value = "/dl/{downloadKey}/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String downloadKey, @PathVariable Integer fileId,
                                                 HttpServletRequest httpServletRequest) throws IOException {
        FileDrop fileDrop = fileDropService.findFileDropDownloadKey(downloadKey);

        if (fileDrop == null || !fileDrop.isValid()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.LOCATION, "/")
                .build();
        }

        if (isDownloadAllowed(fileDrop)) {
            Optional<FileSet> foundFileSet =
                    fileDrop.getFileSet()
                            .stream()
                            .filter(fileSet -> fileSet.getId().equals(fileId))
                            .findFirst();

            if (foundFileSet.isPresent()) {
                Resource resource = storageService.loadAsResource(
                    Paths.get(fileDrop.getDownloadKey(), foundFileSet.get().getId().toString()).toString());

                logger.debug("downloadFile; fileDrop: " + fileDrop + ", fileSet: " + foundFileSet.get());

                if(!currentUser().hasRole(Role.SecurityRole.ADMINISTRATOR)) {
                    Download download = new Download();
                    download.setFileDrop(fileDrop);
                    download.setFileName(foundFileSet.get().getFileName());
                    download.setStarted(LocalDateTime.now());
                    download.setIpAddress(httpServletRequest.getRemoteAddr());
                    download.setStatus("INPROGRESS");
                    download.setCompleted(LocalDateTime.now());
                    downloadRepository.save(download);
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + foundFileSet.get().getFileName() + "\"")
                        .body(resource);
            } else {

                logger.debug("downloadFile; fileDrop: " + fileDrop + ", Could not find fileSet: " + fileId);

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .header(HttpHeaders.LOCATION, "/dl/" + downloadKey)
                        .build();
            }
        }

        logger.debug("downloadFile; Could not find fileDrop with key: " + downloadKey);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header(HttpHeaders.LOCATION, "/dl/" + downloadKey)
                .build();
    }

    public boolean isDownloadAllowed(FileDrop fileDrop) {
        if (!fileDrop.isAuthenticationRequired()) {
            return true;
        }

        return fileDropService.isAuthorized(fileDrop, currentUser().getUsername());
    }

    public User currentUser() {
        return userContextService.getCurrentUser();
    }
}
