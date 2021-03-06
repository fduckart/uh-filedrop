package edu.hawaii.its.filedrop.type;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "allowlist")
public class Allowlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "entry", length = 128)
    private String entry;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "registrant")
    private String registrant;

    @Column(name = "expiration_check")
    private Integer check;

    @Column(name = "expired")
    private Boolean expired;

    // Constructor.
    public Allowlist() {
        // Empty.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getRegistrant() {
        return registrant;
    }

    public void setRegistrant(String registrant) {
        this.registrant = registrant;
    }

    public Integer getCheck() {
        return check;
    }

    public void setCheck(Integer check) {
        this.check = check;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Allowlist [id=" + id
                + ", entry=" + entry
                + ", registrant=" + registrant
                + ", check=" + check
                + ", expired=" + expired
                + ", created=" + created
                + "]";
    }
}
