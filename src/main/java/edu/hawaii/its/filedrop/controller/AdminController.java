package edu.hawaii.its.filedrop.controller;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapPersonEmpty;
import edu.hawaii.its.filedrop.service.LdapService;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Log logger = LogFactory.getLog(AdminController.class);

    @Autowired
    private LdapService ldapService;

    @GetMapping("/admin")
    public String admin() {
        logger.debug("User at admin.");

        return "admin/admin";
    }

    @GetMapping(value = { "/admin/application/role", "/admin/application/roles" })
    public String adminUserRole() {
        logger.debug("User at admin/application/role.");

        return "admin/application-role";
    }

    @GetMapping("/admin/lookup")
    public String adminLookup(Locale locale, Model model) {
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

}