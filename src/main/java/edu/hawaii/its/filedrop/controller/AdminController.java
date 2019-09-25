package edu.hawaii.its.filedrop.controller;

import java.time.LocalDate;
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

import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapPersonEmpty;
import edu.hawaii.its.filedrop.service.LdapService;
import edu.hawaii.its.filedrop.service.MessageService;
import edu.hawaii.its.filedrop.service.WhitelistService;
import edu.hawaii.its.filedrop.type.Message;
import edu.hawaii.its.filedrop.type.Whitelist;

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

    @GetMapping("/admin")
    public String admin() {
        logger.debug("User at admin.");

        return "admin/admin";
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

    @PostMapping("/admin/lookup/ldap")
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

    @GetMapping("/api/admin/whitelist")
    public ResponseEntity<List<Whitelist>> getWhiteList() {
        logger.debug("User at api/admin/whitelist");
        List<Whitelist> whitelist = whitelistService.findAllWhiteList();
        return ResponseEntity.ok(whitelist);
    }

    @PostMapping("/api/admin/whitelist")
    public String addWhitelist(@RequestParam("entry") String entry,
            @RequestParam("registrant") String registrant) {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry(entry);
        whitelist.setEntryName(ldapService.findByUid(entry).getCn());
        whitelist.setRegistrant(registrant);
        whitelist.setRegistrantName(ldapService.findByUid(registrant).getCn());
        whitelist.setCheck(0);
        whitelist.setExpired(false);
        whitelist.setCreated(LocalDate.now());
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

}