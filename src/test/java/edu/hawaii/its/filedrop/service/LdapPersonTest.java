package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LdapPersonTest {

    private LdapPerson ldapPerson;

    @BeforeEach
    public void setUp() {
        ldapPerson = new LdapPerson();
    }

    @Test
    public void accessors() {
        assertNotNull(ldapPerson.getAffiliations());
        assertTrue(ldapPerson.getAffiliations().isEmpty());
        assertNull(ldapPerson.getCn());
        assertThat(ldapPerson.getGivenName(), equalTo(""));
        assertNull(ldapPerson.getMail());
        assertNotNull(ldapPerson.getMails());
        assertTrue(ldapPerson.getMails().isEmpty());
        assertNull(ldapPerson.getSn());
        assertNull(ldapPerson.getTitle());
        assertThat(ldapPerson.getUhUuid(), equalTo(""));
        assertThat(ldapPerson.getUid(), equalTo(""));

        ldapPerson.setGivenName(null);
        assertThat(ldapPerson.getGivenName(), equalTo(""));
        ldapPerson.setGivenName("name");
        assertThat(ldapPerson.getGivenName(), equalTo("name"));

        ldapPerson.setUhUuid(null);
        assertThat(ldapPerson.getUhUuid(), equalTo(""));
        ldapPerson.setUhUuid("12345678");
        assertThat(ldapPerson.getUhUuid(), equalTo("12345678"));

        ldapPerson.setUid(null);
        assertThat(ldapPerson.getUid(), equalTo(""));
        ldapPerson.setUid("username");
        assertThat(ldapPerson.getUid(), equalTo("username"));

        assertThat(ldapPerson.isValid(), equalTo(true));
    }

    @Test
    public void addMail() {
        int count = ldapPerson.getMails().size();
        assertThat(count, equalTo(0));
        ldapPerson.addMail("duckart@example.com");
        assertThat(ldapPerson.getMails().size(), equalTo(count + 1));
        assertThat(ldapPerson.getMails().get(0), equalTo("duckart@example.com"));
    }

    @Test
    public void addAffiliation() {
        int count = ldapPerson.getAffiliations().size();
        assertThat(count, equalTo(0));
        ldapPerson.addAffiliation("system");
        assertThat(ldapPerson.getAffiliations().size(), equalTo(count + 1));
        assertThat(ldapPerson.getAffiliations().get(0), equalTo("system"));
    }

    @Test
    public void testToString() {
        assertThat(ldapPerson.toString(), containsString("LdapPerson ["));

        ldapPerson.setUhUuid("12345678");
        assertThat(ldapPerson.toString(), containsString("uhUuid=12345678"));
    }

}
