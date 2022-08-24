package edu.hawaii.its.filedrop.service;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ldap.core.AttributesMapper;

public class LdapPersonAttributesMapper implements AttributesMapper<LdapPerson> {

    @Override
    public LdapPerson mapFromAttributes(Attributes attrs) {
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

        List<String> orgAffiliations = get(attrs, "uhOrgAffiliation");
        for (String orgAffiliation : orgAffiliations) {
            String affiliation = orgAffiliation.split(",")[1].split("=")[1];
            if (!person.getOrgAffiliations().contains(affiliation)) {
                person.addOrgAffiliation(affiliation);
            }
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
