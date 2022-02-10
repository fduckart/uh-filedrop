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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.AllowlistService;
import edu.hawaii.its.filedrop.service.ApplicationService;
import edu.hawaii.its.filedrop.service.FileDropService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapPersonEmpty;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.service.MessageService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.Allowlist;
import edu.hawaii.its.filedrop.type.Faq;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileDropInfo;
import edu.hawaii.its.filedrop.type.Message;
import edu.hawaii.its.filedrop.type.Setting;
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
    private AllowlistService allowlistService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private FileDropService fileDropService;

    @Autowired
    private ApplicationService applicationService;

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
        message.setTypeId(1);
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

    @GetMapping("/admin/allowlist")
    public String allowlist() {
        logger.debug("User at admin/allowlist.");
        return "admin/allowlist";
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

    @GetMapping("/api/admin/allowlist")
    public ResponseEntity<List<Allowlist>> getAllowList() {
        logger.debug("User at api/admin/allowlist");
        List<Allowlist> allowlist = allowlistService.findAll();
        return ResponseEntity.ok(allowlist);
    }

    @PostMapping("/api/admin/allowlist")
    public ResponseEntity<Allowlist> addAllowlist(@RequestBody Allowlist allowlistBody) {
        Allowlist allowlist = allowlistBody;
        allowlist.setCheck(0);
        allowlist.setExpired(false);
        allowlist.setCreated(LocalDateTime.now());
        allowlist = allowlistService.addAllowlist(allowlist);
        logger.debug("User added Allowlist: " + allowlist);
        return ResponseEntity.ok().body(allowlist);
    }

    @DeleteMapping("/api/admin/allowlist/{allowlistId}")
    public ResponseEntity<Allowlist> deleteAllowlist(@PathVariable Integer allowlistId) {
        Allowlist allowlist = allowlistService.findById(allowlistId);
        allowlistService.deleteAllowlist(allowlist);
        logger.debug("User deleted Allowlist: " + allowlist);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/api/admin/filedrops")
    public ResponseEntity<List<FileDropInfo>> getFileDrops() {
        logger.debug("User at api/admin/filedrops");
        List<FileDropInfo> fileDrops = fileDropService.findAllFileDropsInfo()
                .stream()
                .filter(FileDropInfo::isValid)
                .collect(toList());
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

    @GetMapping("/admin/settings")
    public String settings(Model model) {
        logger.debug("User at admin/settings");
        List<Setting> settings = applicationService.findSettings();
        model.addAttribute("settings", settings);
        return "admin/settings";
    }

    @PostMapping("/admin/settings/{id}")
    public String changeSetting(@PathVariable("id") Integer id,
            @RequestParam("value") String value,
            RedirectAttributes redirectAttributes) {
        Setting setting = applicationService.findSetting(id);
        setting.setValue(value);
        setting = applicationService.saveSetting(setting);
        logger.debug("changeSetting; User changed setting: " + setting);
        redirectAttributes.addFlashAttribute("alert", "Setting modified");
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings")
    public String addSetting(@RequestParam("key") String key,
            @RequestParam("value") String value,
            RedirectAttributes redirectAttributes) {
        Setting setting = new Setting();
        setting.setKey(key);
        setting.setValue(value);
        setting = applicationService.saveSetting(setting);
        applicationService.evictSettingCache();
        logger.debug("addSetting; User created new setting: " + setting);
        redirectAttributes.addFlashAttribute("alert", "Setting added");
        return "redirect:/admin/settings";
    }

    @GetMapping("/admin/faq")
    public String faq() {
        return "admin/faq";
    }

    @PostMapping("/api/admin/faq")
    public ResponseEntity<Faq> createFaq(@RequestBody Faq faq) {
        faq = applicationService.saveFaq(faq);
        logger.debug("createFaq; Created: " + faq);
        return ResponseEntity.ok().body(faq);
    }

    @PostMapping("/api/admin/faq/{id}")
    public ResponseEntity<Faq> editFaq(@PathVariable("id") Integer id,
            @RequestParam("question") String question,
            @RequestParam("answer") String answer) {
        Faq faq = applicationService.findFaq(id);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq = applicationService.saveFaq(faq);
        logger.debug("editFaq; Saved: " + faq);
        return ResponseEntity.ok().body(faq);
    }

    @DeleteMapping("/api/admin/faq/{id}")
    public ResponseEntity<Faq> deleteFaq(@PathVariable("id") Integer id) {
        Faq faq = applicationService.findFaq(id);
        applicationService.deleteFaq(faq);
        logger.debug("deleteFaq; Deleted: " + faq);
        return ResponseEntity.ok().body(faq);
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }

}