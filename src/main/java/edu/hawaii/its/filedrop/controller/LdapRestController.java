package edu.hawaii.its.filedrop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.filedrop.service.LdapPerson;
import edu.hawaii.its.filedrop.service.LdapService;

@RestController
public class LdapRestController {

    @Autowired
    private LdapService ldapService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/api/ldap/{search}")
    public ResponseEntity<LdapPerson> getLdapPerson(@PathVariable String search) {
        LdapPerson ldapPerson = ldapService.findByUhUuidOrUidOrMail(search);
        return ResponseEntity
                .ok()
                .body(ldapPerson);
    }
}
