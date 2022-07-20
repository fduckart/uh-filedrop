package edu.hawaii.its.filedrop.controller;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.service.ProcessVariableHolder;
import edu.hawaii.its.filedrop.service.WorkflowService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Recipient;
import edu.hawaii.its.filedrop.util.Dates;
import edu.hawaii.its.filedrop.util.Strings;

@Controller
public class PrepareController {

    private final Log logger = LogFactory.getLog(PrepareController.class);

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private EmailService emailService;

    @Value("${app.mail.help}")
    private String helpName;

    @Value("${app.mail.to.help}")
    private String helpEmail;

    @Value("${app.max.size}")
    private String maxUploadSize;

    @GetMapping(value = "/helpdesk/files/{uploadKey}")
    public String addFileHelpDesk(Model model, @PathVariable String uploadKey) {
        model.addAttribute("maxUploadSize", maxUploadSize);
        model.addAttribute("uploadKey", uploadKey);
        model.addAttribute("recipients", Arrays.asList(helpName));

        return "user/files-helpdesk";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = "/prepare/files/{uploadKey}")
    public String addFiles(Model model, @PathVariable String uploadKey) {

        logger.debug("User at addFiles.");
        User user = currentUser();

        if (workflowService.atTask(user, "addRecipients")) {
            return "redirect:/prepare";
        }

        FileDrop filedrop = fileDropService.findFileDropUploadKey(uploadKey);
        if (logger.isDebugEnabled()) {
            logger.debug("addFiles; filedrop: " + filedrop);
        }

        ProcessVariableHolder processVariableHolder =
                new ProcessVariableHolder(workflowService.getProcessVariables(user));

        String[] recipients = processVariableHolder.getStrings("recipients");

        List<String> recipientsList = Arrays.stream(recipients).map(recipient -> {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);
            if (ldapPerson.isValid()) {
                return ldapPerson.getCn();
            }
            return recipient;
        }).collect(toList());

        model.addAttribute("recipients", recipientsList);
        model.addAttribute("maxUploadSize", maxUploadSize);
        model.addAttribute("uploadKey", filedrop.getUploadKey());
        model.addAttribute("authentication", filedrop.isAuthenticationRequired());

        return "user/files";
    }

    @PostMapping(value = "/helpdesk")
    public String addHelpdesk(@RequestParam String sender,
                              @RequestParam Integer expiration,
                              @RequestParam(required = false) Integer ticketNumber,
                              RedirectAttributes redirectAttributes) {

        FileDrop fileDrop = new FileDrop();
        fileDrop.setUploader(sender);
        fileDrop.setUploaderFullName(sender);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setEncryptionKey("aes:" + Strings.generateRandomString());
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop.setUploadKey(Strings.generateRandomString());
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now());

        fileDrop = fileDropService.saveFileDrop(fileDrop);

        fileDropService.addRecipients(fileDrop, helpEmail);

        logger.debug("Sender: " + sender);
        logger.debug("Recipient: " + fileDrop.getRecipients());
        logger.debug("Expiration: " + expiration);
        logger.debug("Ticket Number: " + ticketNumber);

        logger.debug("Upload Key: " + fileDrop.getUploadKey());

        redirectAttributes.addAttribute("uploadKey", fileDrop.getUploadKey())
                .addFlashAttribute("expiration", expiration)
                .addFlashAttribute("ticketNumber", ticketNumber);

        return "redirect:/helpdesk/files/{uploadKey}";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare")
    public String addRecipients(@RequestParam("sender") String sender,
                                @RequestParam("validation") Boolean validation,
                                @RequestParam("expiration") Integer expiration,
                                @RequestParam("recipients") String[] recipients,
                                @RequestParam("message") String message) {

        User user = currentUser();
        boolean hasFileDrop = workflowService.hasFileDrop(user);

        if (logger.isDebugEnabled()) {
            logger.debug("addRecipients; user: " + user);
            logger.debug("addRecipients; hasFileDrop: " + hasFileDrop);
            logger.debug("addRecipients;  recipients: " + Arrays.toString(recipients));
        }

        FileDrop fileDrop;

        if (hasFileDrop) {
            fileDrop = fileDropService.findFileDrop(fileDropService.getFileDropId(user));
            fileDrop.setAuthenticationRequired(validation);
        } else {
            fileDrop = new FileDrop();
            fileDrop.setEncryptionKey("aes:" + Strings.generateRandomString());
            fileDrop.setDownloadKey(Strings.generateRandomString());
            fileDrop.setUploadKey(Strings.generateRandomString());
            fileDrop.setUploader(user.getUsername());
            fileDrop.setUploaderFullName(user.getName());
            fileDrop.setAuthenticationRequired(validation);
        }
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now());
        fileDrop.setValid(true);
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        if (logger.isDebugEnabled()) {
            logger.debug("addRecipients; fileDrop saved: " + fileDrop);
        }

        ProcessVariableHolder processVariableHolder = new ProcessVariableHolder();
        processVariableHolder.add("fileDropId", fileDrop.getId());
        processVariableHolder.add("fileDropDownloadKey", fileDrop.getDownloadKey());
        processVariableHolder.add("expirationLength", expiration);
        processVariableHolder.add("sender", sender);
        processVariableHolder.add("message", message);

        workflowService.addProcessVariables(user, processVariableHolder);

        fileDropService.addRecipients(user, recipients);

        for (String recipient : recipients) {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient);
            boolean checkRecipient = fileDropService.checkRecipient(user, ldapPerson, fileDrop.isAuthenticationRequired());
            logger.debug("addRecipients; checkRecipient => " + recipient + ": " + checkRecipient);
            if (!checkRecipient) {
                logger.debug("addRecipients; redirect to /prepare ...");
                return "redirect:/prepare";
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("addRecipients; User '" + user.getUsername() + "' created " + fileDrop);
            logger.debug("addRecipients; Sender: " + sender);
            logger.debug("addRecipients; returning...");
        }
        return "redirect:/prepare/files/" + fileDrop.getUploadKey();
    }

    @GetMapping(value = "/complete/{uploadKey}")
    public String completeFileDrop(@PathVariable String uploadKey) throws IOException, GeneralSecurityException {
        logger.debug("completeFileDrop; start.");
        logger.info("completeFileDrop; uploadKey: " + uploadKey);

        if ("off".equals("")) {
            throw new NullPointerException("Stop the Music!");
        }

        Task currentTask = workflowService.currentTask(currentUser());
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);

        boolean isUploader = fileDrop.getUploader().equals(currentUser().getUsername());
        logger.debug("completeFileDrop; isUploader: " + isUploader);
        if (!isUploader) {
            return "redirect:/dl/" + fileDrop.getDownloadKey();
        }

        ProcessVariableHolder processVariables =
                new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));

        Integer expiration = processVariables.getInteger("expirationLength");
        String[] recipients = processVariables.getStrings("recipients");

        LocalDateTime now = LocalDateTime.now();
        fileDrop.setCreated(now);
        fileDrop.setExpiration(Dates.addMinutes(now, expiration));
        fileDrop.setValid(true);
        fileDrop = fileDropService.saveFileDrop(fileDrop);

        if (logger.isDebugEnabled()) {
            logger.debug("completeFileDrop; fileDrop: " + fileDrop);
            logger.debug("completeFileDrop; recipients: " + Arrays.asList(recipients));
        }

        fileDropService.addRecipients(fileDrop, recipients);

        String sender = processVariables.getString("sender");
        Mail mail = new Mail();
        mail.setTo(sender);
        mail.setFrom(emailService.getFrom());

        logger.debug("Sending email to uploader ... " + mail);
        Map<String, Object> fileDropContext = emailService.getFileDropContext("uploader", fileDrop);
        emailService.send(mail, "uploader", fileDropContext);

        mail.setFrom(sender);

        long size = totalFilesize(fileDrop);

        for (Recipient recipient : fileDropService.findRecipients(fileDrop)) {
            LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(recipient.getName());

            if (ldapPerson.isValid()) {
                mail.setTo(ldapPerson.getMails().get(0));
            } else {
                mail.setTo(recipient.getName());
            }

            fileDropContext = emailService.getFileDropContext("receiver", fileDrop);
            fileDropContext.put("comment", processVariables.getString("message"));
            fileDropContext.put("size", size);
            fileDropContext.put("sender", sender);
            logger.debug("Sending email to receiver ... " + mail);
            emailService.send(mail, "receiver", fileDropContext);
        }

        fileDropService.completeFileDrop(currentUser(), fileDrop);

        logger.debug("completeFileDrop; done.");

        return "redirect:/dl/" + fileDrop.getDownloadKey();
    }

    private long totalFilesize(FileDrop fileDrop) {
        long size = 0;
        for (FileSet fileSet : fileDrop.getFileSet()) {
            size += fileSet.getSize();
        }
        return size;
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }

    @GetMapping(value = "/helpdesk/successful/{uploadKey}")
    public String helpdeskSuccessful(RedirectAttributes redirectAttributes,
                                     @PathVariable String uploadKey,
                                     @RequestParam String expiration,
                                     @RequestParam(required = false) String ticketNumber) {
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        LocalDateTime now = LocalDateTime.now();
        fileDrop.setCreated(now);
        fileDrop.setExpiration(Dates.addMinutes(now, Integer.parseInt(expiration)));
        fileDrop.setValid(true);

        Mail mail = new Mail();
        mail.setTo(helpEmail);
        mail.setFrom(fileDrop.getUploader());
        mail.setSubject("FileDrop Helpdesk Ticket: " + ticketNumber);

        redirectAttributes.addFlashAttribute("message", "File(s) uploaded <strong>successfully</strong>");
        return "redirect:/";
    }

    @PreAuthorize("hasRole('UH')")
    @GetMapping(value = { "/prepare", "/prepare/" })
    public String prepare(Model model, @RequestParam(value = "expiration", required = false) Integer defaultExpiration) {

        if (logger.isDebugEnabled()) {
            logger.debug("prepare; defaultExpiration: " + defaultExpiration);
        }

        if ("off".equals("")) {
            throw new NullPointerException("Hey Now!");
        }

        User user = currentUser();
        Task currentTask = workflowService.currentTask(user);
        if (logger.isDebugEnabled()) {
            logger.debug("prepare; currentUser: " + user);
            logger.debug("prepare; currentTask: " + currentTask);
        }

        if (currentTask != null && currentTask.getTaskDefinitionKey().equalsIgnoreCase("filesTask")) {
            Integer fileDropId = fileDropService.getFileDropId(user);
            FileDrop fileDrop = fileDropService.findFileDrop(fileDropId);

            if (logger.isDebugEnabled()) {
                logger.debug("prepare; fileDropId: " + fileDropId);
                logger.debug("prepare; revertTask...");
            }
            workflowService.revertTask(user, "recipientsTask");

            ProcessVariableHolder processVariableHolder =
                    new ProcessVariableHolder(workflowService.getProcessVariables(currentTask));
            model.addAttribute("sender", processVariableHolder.getString("sender"));
            model.addAttribute("expiration", processVariableHolder.getInteger("expirationLength"));
            model.addAttribute("authentication", fileDrop.isAuthenticationRequired());
            model.addAttribute("recipients", processVariableHolder.getStrings("recipients"));
            model.addAttribute("message", processVariableHolder.getString("message"));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("prepare; start upload process ");
            }
            fileDropService.startUploadProcess(user);
            model.addAttribute("expiration", defaultExpiration);
        }

        model.addAttribute("uid", user.getUid());
        model.addAttribute("cn", user.getName());
        model.addAttribute("affiliations", user.getAttributes().getAffiliation());
        model.addAttribute("mails", user.getAttributes().getMail());
        model.addAttribute("mail", user.getAttribute("uhMail"));

        if (logger.isDebugEnabled()) {
            logger.debug("prepare; user: " + user);
            logger.debug("prepare; currentTask: " + currentTask);
            logger.debug("prepare; returning...");
        }

        return "user/prepare";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = { "/prepare/recipient/add" })
    public ResponseEntity<?> addRecipient(@RequestParam("recipient") String user,
                                          @RequestParam("authenticationRequired") Boolean authRequired) {
        LdapPerson person = ldapService.findByUhUuidOrUidOrMail(user);
        User currentUser = currentUser();
        logger.debug(currentUser.getUid() + " looked for " + user + " and found " + person);

        if (!person.isValid() && authRequired) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(Collections.singletonMap("message",
                            "Could not add non-UH recipient when authentication is required."));
        }

        if (fileDropService.checkRecipient(currentUser, person, authRequired)) {
            return ResponseEntity.ok().body(person);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Collections.singletonMap("message",
                        "Could not add recipient due to restrictions."));
    }

    @GetMapping(value = "/helpdesk")
    public String prepareHelpdesk(Model model) {
        logger.debug("User at prepare-helpdesk");
        model.addAttribute("recipient", helpName);
        model.addAttribute("recipientEmail", helpEmail);
        return "user/prepare-helpdesk";
    }

    @PreAuthorize("hasRole('UH')")
    @PostMapping(value = "/prepare/files/{uploadKey}")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFiles(@RequestParam MultipartFile file,
                            @RequestParam String comment,
                            @PathVariable String uploadKey)
            throws IOException, GeneralSecurityException {
        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);
        fileDropService.uploadFile(currentUser(), file, comment, fileDrop);
    }

    @PostMapping(value = "/helpdesk/files/{uploadKey}")
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadFilesHelpdesk(@PathVariable String uploadKey,
                                    @RequestParam MultipartFile file,
                                    @RequestParam("comment") String comment) {
        FileSet fileSet = new FileSet();
        fileSet.setFileName(file.getOriginalFilename());
        fileSet.setType(file.getContentType());
        fileSet.setComment(comment);
        fileSet.setSize(file.getSize());

        FileDrop fileDrop = fileDropService.findFileDropUploadKey(uploadKey);

        fileSet.setFileDrop(fileDrop);
        fileDropService.saveFileSet(fileSet);

        if (logger.isDebugEnabled()) {
            logger.debug("uploadFilesHelpdesk; uploadKey: " + uploadKey);
            logger.debug("uploadFilesHelpdesk;    uploader: " + fileDrop.getUploader());
            logger.debug("uploadFilesHelpdesk;     fileSet: " + fileSet);
            logger.debug("uploadFilesHelpdesk;    fileDrop: " + fileDrop);
        }
    }
}
