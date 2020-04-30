package edu.hawaii.its.filedrop.controller;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.repository.DownloadRepository;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapPersonEmpty;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.service.MessageService;
import edu.hawaii.its.filedrop.service.WhitelistService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileDropInfo;
import edu.hawaii.its.filedrop.type.Message;
import edu.hawaii.its.filedrop.type.Whitelist;
import edu.hawaii.its.filedrop.util.Dates;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Log logger = LogFactory.getLog(AdminController.class);

    @Autowired
    private LdapService ldapService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private DownloadRepository downloadRepository;

    @GetMapping("/admin")
    public String admin() {
        logger.debug("User at admin.");

        return "admin/admin";
    }

    @GetMapping(value = "/admin/fonts")
    public String fonts() {
        logger.debug("User at admin/fonts.");
        return "admin/fonts";
    }

    @GetMapping("/admin/technology")
    public String technology() {
        logger.debug("User at admin/technology.");

        return "admin/technology";
    }

    @GetMapping("/admin/gate-message")
    public String gateMessage(Model model) {
        int messageId = Message.GATE_MESSAGE;
        Message message = messageService.findMessage(messageId);
        model.addAttribute("message", message);
        return "admin/gate-message";
    }

    @PostMapping(value = "/admin/gate-message", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String setGateMessage(Model model, Message message) {
        messageService.update(message);
        messageService.evictCache();
        model.addAttribute("success", true);
        return "admin/gate-message";
    }

    @GetMapping("/admin/icons")
    public String icons() {
        logger.debug("User at admin/icons.");
        return "admin/icons";
    }

    @GetMapping(value = { "/admin/application/role", "/admin/application/roles" })
    public String adminUserRole() {
        logger.debug("User at admin/application/role.");

        return "admin/application-role";
    }

    @GetMapping("/admin/lookup")
    public String adminLookup(Model model) {
        logger.debug("User at admin/lookup.");
        model.addAttribute("person", new LdapPersonEmpty());

        return "admin/lookup";
    }

    @PostMapping("/admin/lookup")
    public String adminLookupLdap(Model model, @ModelAttribute("search") String search) {
        logger.debug("User at admin/ldap/data.");

        LdapPerson person = ldapService.findByUhUuidOrUidOrMail(search);
        model.addAttribute("person", person);

        return "admin/lookup";
    }

    @GetMapping("/admin/whitelist")
    public String whitelist() {
        logger.debug("User at admin/whitelist.");
        return "admin/whitelist";
    }

    @GetMapping("/admin/email")
    public String email() {
        logger.debug("User at admin/email.");
        return "admin/emails";
    }

    @PostMapping("/admin/email")
    public String sendEmailTemplate(@RequestParam("template") String template) {
        Mail mail = new Mail();
        mail.setFrom(emailService.getFrom());
        mail.setTo(currentUser().getAttributes().getValue("uhEmail"));
        emailService.send(mail, template, new Context());
        return "redirect:/admin/email";
    }

    @GetMapping("/api/admin/whitelist")
    public ResponseEntity<List<Whitelist>> getWhiteList() {
        logger.debug("User at api/admin/whitelist");
        List<Whitelist> whitelist = whitelistService.findAllWhiteList();
        return ResponseEntity.ok(whitelist);
    }

    @PostMapping("/api/admin/whitelist")
    public String addWhitelist(@RequestParam("entry") String entry, @RequestParam("registrant") String registrant) {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry(entry);
        whitelist.setEntryName(ldapService.findByUid(entry).getCn());
        whitelist.setRegistrant(registrant);
        whitelist.setRegistrantName(ldapService.findByUid(registrant).getCn());
        whitelist.setCheck(0);
        whitelist.setExpired(false);
        whitelist.setCreated(LocalDateTime.now());
        whitelist = whitelistService.addWhitelist(whitelist);
        logger.debug("User added Whitelist: " + whitelist);
        return "redirect:/admin/whitelist";
    }

    @DeleteMapping("/api/admin/whitelist/{whitelistId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteWhitelist(@PathVariable Integer whitelistId) {
        Whitelist whitelist = whitelistService.findWhiteList(whitelistId);
        whitelistService.deleteWhitelist(whitelist);
        logger.debug("User deleted Whitelist: " + whitelist);
    }

    @GetMapping("/api/admin/filedrops")
    public ResponseEntity<List<FileDropInfo>> getFileDrops() {
        logger.debug("User at api/admin/filedrops");
        List<FileDropInfo> fileDrops = fileDropService.findAllFileDrop().stream().filter(FileDrop::isValid).map(fileDrop -> {
            FileDropInfo fileDropInfo = new FileDropInfo();
            fileDropInfo.setUploader(fileDrop.getUploader());
            fileDropInfo.setCreated(fileDrop.getCreated());
            fileDropInfo.setExpiration(fileDrop.getExpiration());
            fileDropInfo.setFileDropId(fileDrop.getId());
            fileDropInfo.setFileInfoList(fileDrop.getFileSet().stream().map(fileSet -> {
                FileDropInfo.FileInfo fileInfo = new FileDropInfo.FileInfo();
                fileInfo.setFileName(fileSet.getFileName());
                fileInfo.setFileSize(fileSet.getSize());
                fileInfo.setFileType(fileSet.getType());
                fileInfo.setDownloads(downloadRepository.findAllByFileDropAndFileName(fileDrop, fileSet.getFileName()).size());
                return fileInfo;
            }).collect(toList()));
            fileDropInfo.setDownloads(fileDropInfo.getFileInfoList().stream().mapToInt(FileDropInfo.FileInfo::getDownloads).sum());
            return fileDropInfo;
        }).collect(toList());
        return ResponseEntity.ok().body(fileDrops);
    }

    @GetMapping("/admin/dashboard")
    public String getDashboard() {
        logger.debug("User at admin/dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/admin/add-expiration/{id}/{amount}")
    public String addExpiration(@PathVariable Integer id, @PathVariable Integer amount,
            RedirectAttributes redirectAttributes) {
        FileDrop fileDrop = fileDropService.findFileDrop(id);
        fileDrop.setExpiration(Dates.add(fileDrop.getExpiration(), amount));
        fileDropService.saveFileDrop(fileDrop);
        logger.debug(currentUser().getUsername() + " added " + amount + " days to: " + fileDrop);
        redirectAttributes.addFlashAttribute("addExpiration", amount);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/expire/{id}")
    public String expireFileDrop(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        FileDrop fileDrop = fileDropService.findFileDrop(id);
        fileDropService.expire(fileDrop);
        logger.debug("admin; " + currentUser().getUsername() + " expired " + fileDrop);
        redirectAttributes.addFlashAttribute("expired", true);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/permissions")
    public String permissions() {
        return "admin/permissions";
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }

}