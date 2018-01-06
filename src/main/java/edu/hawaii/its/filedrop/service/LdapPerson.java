package edu.hawaii.its.filedrop.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.hawaii.its.filedrop.type.PersonIdentifiable;

public class LdapPerson implements PersonIdentifiable {

    private String cn;
    private String sn;
    private String givenName;
    private Object mail;
    private String uhUuid;
    private String uid;
    private String title;
    private List<String> emails = new ArrayList<>();
    private List<String> affiliations = new ArrayList<>();

    public void addMail(String mail) {
        emails.add(mail);
    }

    public void addAffiliation(String affiliation) {
        affiliations.add(affiliation);
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getGivenName() {
        return givenName != null ? givenName : "";
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public Object getMail() {
        return mail;
    }

    public void setMail(Object mail) {
        this.mail = mail;
    }

    @Override
    public String getUhUuid() {
        return uhUuid != null ? uhUuid : "";
    }

    public void setUhUuid(String uhUuid) {
        this.uhUuid = uhUuid;
    }

    public boolean isValid() {
        return getUhUuid().length() > 0;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAffiliations() {
        return Collections.unmodifiableList(affiliations);
    }

    public List<String> getMails() {
        return Collections.unmodifiableList(emails);
    }

    @Override
    public String toString() {
        return "LdapPerson ["
                + "cn=" + cn
                + ", sn=" + sn
                + ", givenName=" + givenName
                + ", uhUuid=" + uhUuid
                + ", uid=" + uid
                + ", mail=" + emails
                + ", affiliations=" + affiliations
                + "]";
    }

}
