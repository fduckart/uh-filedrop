package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.junit.Before;
import org.junit.Test;

public class LdapPersonAttributesMapperTest {

    private LdapPersonAttributesMapper ldapPersonAttributesMapper;

    @Before
    public void setUp() {
        ldapPersonAttributesMapper = new LdapPersonAttributesMapper();
    }

    @Test
    public void affiliations() throws NamingException {
        Attributes attrs = new BasicAttributes(true);
        attrs.put("mail", "some@example.com");
        attrs.put("cn", "Jane Doe");
        Attribute attr = new BasicAttribute("eduPersonAffiliation");
        attr.add("kirk");
        attr.add("spock");
        attr.add("mccoy");
        attrs.put(attr);

        LdapPerson person = ldapPersonAttributesMapper.mapFromAttributes(attrs);
        assertNotNull(person);
        assertThat(person.getMails().size(), equalTo(1));
        assertThat(person.getMails().get(0), equalTo("some@example.com"));
        assertThat(person.getCn(), equalTo("Jane Doe"));

        assertThat(person.getAffiliations().size(), equalTo(3));
        assertThat(person.getAffiliations(), hasItem("kirk"));
        assertThat(person.getAffiliations(), hasItem("spock"));
        assertThat(person.getAffiliations(), hasItem("mccoy"));
    }

    @Test
    public void get() throws NamingException {
        Attributes attrs = new BasicAttributes(true);
        attrs.put("what", "some@example.com");
        Attribute attr = new BasicAttribute("someAttribute");
        attr.add("mccoy");
        attrs.put(attr);

        LdapPerson person = ldapPersonAttributesMapper.mapFromAttributes(attrs);
        assertNotNull(person);
        assertFalse(person.isValid());
    }
}
