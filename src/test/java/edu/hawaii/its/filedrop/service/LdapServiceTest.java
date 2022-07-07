package edu.hawaii.its.filedrop.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class LdapServiceTest {

    @Value("ldap.search.attributes")
    private String[] searchAttributes;

    @Autowired
    private LdapService ldapService;

    @Test
    public void setUp() {
        assertNotNull(ldapService);
    }

    @Test
    public void findByMail() {
        String email = "beno@example.com";
        LdapPerson person = ldapService.findByMail(email);
        assertNotNull(person);
        assertThat(person.getGivenName(), equalTo("Brian"));
        assertThat(person.getSn(), equalTo("Eno"));
        assertThat(person.getCn(), equalTo("Brian Eno"));
        assertThat(person.getUhUuid(), equalTo("10000001"));
        assertThat(person.getMails().size(), equalTo(1));
        assertThat(person.getMails().get(0), equalTo(email));
    }

    @Test
    public void findByCnContains() {
        String name = "Rich";
        List<LdapPerson> people = ldapService.findByCnContains(name);

        assertFalse(people.isEmpty());
        assertTrue(people.get(0).getCn().contains(name));
        assertTrue(people.get(1).getCn().contains(name));
        assertNotEquals(people.get(0).getCn(), people.get(1).getCn());

        assertThat(people.get(0).getGivenName(), equalTo("Keith"));
        assertThat(people.get(0).getSn(), equalTo("Richards"));

        assertThat(people.get(1).getGivenName(), equalTo("Richard"));
        assertThat(people.get(1).getSn(), equalTo("Thompson"));
    }

    @Test
    public void findByCnContainsEmptyResult() {
        String name = "-not-found-";
        List<LdapPerson> people = ldapService.findByCnContains(name);

        assertTrue(people.isEmpty());
    }

    @Test
    public void findByMailNotFound() {
        String email = "notfound@hawaii.edu";
        LdapPerson person = ldapService.findByMail(email);
        assertNotNull(person);
        assertThat(person.getClass(), equalTo(LdapPersonEmpty.class));
    }

    @Test
    public void findByUidOrMail() {
        String email = "beno@example.com";
        LdapPerson person = ldapService.findByUidOrMail(email);
        assertNotNull(person);
        assertThat(person.getGivenName(), equalTo("Brian"));
        assertThat(person.getSn(), equalTo("Eno"));
        assertThat(person.getCn(), equalTo("Brian Eno"));
        assertThat(person.getUhUuid(), equalTo("10000001"));
        assertThat(person.getMails().size(), equalTo(1));
        assertThat(person.getMails().get(0), equalTo(email));
    }

    @Test
    public void findByUhUuid() {
        final String uhUuid = "10000001";
        LdapPerson person = ldapService.findByUhUuid(uhUuid);
        assertThat(person.getGivenName(), equalTo("Brian"));
        assertThat(person.getSn(), equalTo("Eno"));
        assertThat(person.getCn(), equalTo("Brian Eno"));
        assertThat(person.getUhUuid(), equalTo("10000001"));
        assertThat(person.getMails().size(), equalTo(1));
        assertThat(person.getMails().get(0), equalTo("beno@example.com"));
    }

    @Test
    public void findByUhUuid2() {
        String uhUuid = "10000001";
        LdapPerson person = ldapService.findByUhUuid(uhUuid);
        assertThat(person.getGivenName(), equalTo("Brian"));
        assertThat(person.getSn(), equalTo("Eno"));
        assertThat(person.getCn(), equalTo("Brian Eno"));
        assertThat(person.getUhUuid(), equalTo(uhUuid));
        assertThat(person.getMails().size(), equalTo(1));
        assertThat(person.getMails().get(0), equalTo("beno@example.com"));
        uhUuid = "11111111";
        person = ldapService.findByUhUuid(uhUuid);
        assertThat(person.getGivenName(), equalTo("John"));
        assertThat(person.getSn(), equalTo("Lennon"));
        assertThat(person.getCn(), equalTo("John W Lennon"));
        assertThat(person.getUhUuid(), equalTo(uhUuid));
        assertThat(person.getMails().size(), equalTo(1));
        assertThat(person.getMails().get(0), equalTo("jwlennon@hawaii.edu"));
    }

    @Test
    public void findByUhUuidNotFound() {
        final String uhUuid = "99999999";
        LdapPerson person = ldapService.findByUhUuid(uhUuid);
        assertFalse(person.isValid());
    }

    @Test
    public void findByUidOrMailNotFound() {
        String email = "notfound@hawaii.edu";
        LdapPerson person = ldapService.findByUidOrMail(email);
        assertNotNull(person);
        assertThat(person.getClass(), equalTo(LdapPersonEmpty.class));
    }

    @Test
    public void findByUhUuidOrUidOrMail() {
        String[] values = {
                "krichards",
                "krichards@example.com",
                "keith.richards@example.com"
        };

        for (String value : values) {
            LdapPerson person = ldapService.findByUhUuidOrUidOrMail(value);

            assertNotNull(person);
            assertThat(person.getGivenName(), equalTo("Keith"));
            assertThat(person.getSn(), equalTo("Richards"));
            assertThat(person.getCn(), equalTo("Keith Richards"));
            assertThat(person.getUhUuid(), equalTo("10000002"));
            assertThat(person.getMails().size(), equalTo(2));
            assertThat(person.getMails(), hasItem("krichards@example.com"));
            assertThat(person.getMails(), hasItem("keith.richards@example.com"));
        }

        values = new String[] {
                "rthompson",
                "rthompson@example.com",
                "beeswing@example.com"
        };

        for (String value : values) {
            LdapPerson person = ldapService.findByUhUuidOrUidOrMail(value);

            assertNotNull(person);
            assertThat(person.getGivenName(), equalTo("Richard"));
            assertThat(person.getSn(), equalTo("Thompson"));
            assertThat(person.getCn(), equalTo("Richard Thompson"));
            assertThat(person.getUhUuid(), equalTo("10000004"));
            assertThat(person.getMails().size(), equalTo(2));
            assertThat(person.getMails(), hasItem("rthompson@example.com"));
            assertThat(person.getMails(), hasItem("beeswing@example.com"));
        }
    }

    @Test
    public void findByUhUuidOrUidOrMailNotFound() {
        String email = "notfound@hawaii.edu";
        LdapPerson person = ldapService.findByUhUuidOrUidOrMail(email);
        assertNotNull(person);
        assertThat(person.getClass(), equalTo(LdapPersonEmpty.class));
    }

    @Test
    public void findByUid() {
        String uid = "krichards";
        LdapPerson person = ldapService.findByUid(uid);

        assertNotNull(person);
        assertThat(person.getGivenName(), equalTo("Keith"));
        assertThat(person.getSn(), equalTo("Richards"));
        assertThat(person.getCn(), equalTo("Keith Richards"));
        assertThat(person.getMails().size(), equalTo(2));
        assertThat(person.getMails(), hasItem("krichards@example.com"));
        assertThat(person.getMails(), hasItem("keith.richards@example.com"));

        uid = "rthompson";
        person = ldapService.findByUid(uid);

        assertNotNull(person);
        assertThat(person.getGivenName(), equalTo("Richard"));
        assertThat(person.getSn(), equalTo("Thompson"));
        assertThat(person.getCn(), equalTo("Richard Thompson"));
        assertThat(person.getMails().size(), equalTo(2));
        assertThat(person.getMails(), hasItem("rthompson@example.com"));
        assertThat(person.getMails(), hasItem("beeswing@example.com"));
    }

    @Test
    public void findByUidNotFound() {
        String uid = "notfound";
        LdapPerson person = ldapService.findByUid(uid);
        assertNotNull(person);
        assertThat(person.getClass(), equalTo(LdapPersonEmpty.class));
    }

    @Test
    public void testToString() {
        String s = ldapService.toString();
        assertThat(s, containsString("LdapService ["));
    }
}
