package edu.hawaii.its.filedrop.service;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class LdapPersonAttributesMapper implements AttributesMapper<LdapPerson> {

    @Override
    public LdapPerson mapFromAttributes(Attributes attrs) throws NamingException {
        LdapPerson person = new LdapPerson();
        person.setCn(get(attrs.get("cn")));
        person.setSn(get(attrs.get("sn")));
        person.setUhUuid(get(attrs.get("uhUuid")));
        person.setUid(get(attrs.get("uid")));
        person.setGivenName(get(attrs.get("givenName")));
        person.setTitle(get(attrs.get("title")));

        List<String> emails = get(attrs, "mail");
        for (String email : emails) {
            person.addMail(email);
        }

        List<String> affiliations = get(attrs, "eduPersonAffiliation");
        for (String affiliation : affiliations) {
            person.addAffiliation(affiliation);
        }

        return person;
    }

    private String get(Attribute attr) {
        String result = null;
        try {
            result = (String) attr.get();
        } catch (Exception e) {
            // Ignored for now.
        }
        return result != null ? result : "";
    }

    private List<String> get(Attributes attrs, String attributeName) {
        List<String> results = new ArrayList<String>();
        try {
            NamingEnumeration<?> e = attrs.get(attributeName).getAll();
            while (e.hasMoreElements()) {
                String v = (String) e.nextElement();
                results.add(v);
            }
        } catch (Exception e) {
            // Ignored.
        }
        return results;
    }
}
