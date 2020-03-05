package edu.hawaii.its.filedrop.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;

@RestController
public class LdapRestController {

    private static final Log logger = LogFactory.getLog(LdapRestController.class);

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private LdapService ldapService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/api/ldap/{search}")
    public ResponseEntity<LdapPerson> findLdapPerson(@PathVariable String search) {
        LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(search);
        logger.debug(userContextService.getCurrentUsername() + " searched " + search + " and found " + ldapPerson);
        return ResponseEntity
                .ok()
                .body(ldapPerson);
    }
}
