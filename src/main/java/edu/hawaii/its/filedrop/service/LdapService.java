package edu.hawaii.its.filedrop.service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${ldap.search.attributes}")
    private String[] searchAttributes;

    // Constructor.
    @Autowired
    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @PostConstruct
    public void init() {
        Assert.notNull(ldapTemplate, "'ldapTemplate' should not be null.");
    }

    public LdapPerson findByMail(String mail) {
        LdapPersonAttributesMapper mapper = new LdapPersonAttributesMapper();
        List<LdapPerson> list = ldapTemplate.search(queryByMail(mail), mapper);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new LdapPersonEmpty();
    }

    public LdapPerson findByUid(String uid) {
        LdapPersonAttributesMapper mapper = new LdapPersonAttributesMapper();
        List<LdapPerson> list = ldapTemplate.search(queryByUid(uid), mapper);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new LdapPersonEmpty();
    }

    public LdapPerson findByUhUuid(String uhUuid) {
        LdapPersonAttributesMapper mapper = new LdapPersonAttributesMapper();
        List<LdapPerson> list = ldapTemplate.search(queryByUhUuid(uhUuid), mapper);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new LdapPersonEmpty();
    }

    public LdapPerson findByUidOrMail(String value) {
        LdapPersonAttributesMapper mapper = new LdapPersonAttributesMapper();
        List<LdapPerson> list = ldapTemplate.search(queryByUidOrMail(value), mapper);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new LdapPersonEmpty();
    }

    public LdapPerson findByUhUuidOrUidOrMail(String value) {
        LdapPersonAttributesMapper mapper = new LdapPersonAttributesMapper();
        List<LdapPerson> list = ldapTemplate.search(queryByUidOrMailOrUhUuid(value), mapper);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new LdapPersonEmpty();
    }

    public List<LdapPerson> findByCnConstains(String value) {
        LdapPersonAttributesMapper mapper = new LdapPersonAttributesMapper();
        LdapQuery ldapQuery = queryByCnContains(value);
        List<LdapPerson> list = ldapTemplate.search(ldapQuery, mapper);
        if (!list.isEmpty()) {
            return list;
        }
        return new LinkedList<>();
    }

    private LdapQuery queryByCnContains(String value) {
        return query().where("objectclass").is("person")
                .and("cn").like("*" + value + "*") // like, we will filter that cn starts with value 
                .and("mail").isPresent() // must have email address for Google Drive permissions
                .and("uhUuid").isPresent() // Campus entities and others do not have uhUuid
                .and("cn").not().like("*--temp"); // temp entries end with --temp
    }

    private LdapQuery queryByUidOrMailOrUhUuid(String value) {
        return query().where("objectclass").is("person")
                .and("mail").is(value)
                .or(query().where("uid").is(value))
                .or(query().where("uhuuid").is(value));
    }

    private LdapQuery queryByUidOrMail(String value) {
        return query().where("objectclass").is("person")
                .and("mail").is(value)
                .or(query().where("uid").is(value));
    }

    private LdapQuery queryByMail(String mail) {
        return query()
                .timeLimit(200)
                .countLimit(1)
                .attributes(searchAttributes)
                .where("objectclass").is("person")
                .and("mail").is(mail);
    }

    private LdapQuery queryByUid(String uid) {
        return query()
                .timeLimit(200)
                .countLimit(1)
                .attributes(searchAttributes)
                .where("objectclass").is("person")
                .and("uid").is(uid);
    }

    private LdapQuery queryByUhUuid(String uhUuid) {
        return query()
                .timeLimit(200)
                .countLimit(1)
                .attributes(searchAttributes)
                .where("objectclass").is("person")
                .and("uhuuid").is(uhUuid);
    }

    @Override
    public String toString() {
        return "LdapService ["
                + "searchAttributes=" + Arrays.toString(searchAttributes)
                + ", ldapTemplate=" + ldapTemplate + "]";
    }

}
