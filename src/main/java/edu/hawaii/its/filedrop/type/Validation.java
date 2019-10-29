package edu.hawaii.its.filedrop.type;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "validation")
public class Validation {

    @Column(name = "address", length = 64, nullable = false)
    private String address;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Id
    @Column(name = "vkey", length = 64, nullable = false, unique = true)
    private String validationKey;

    @Column(name = "created", nullable = false)
    private Timestamp created;

    @Column(name = "ip_addr", length = 32, nullable = false)
    private String ipAddress;

    //Constructor
    public Validation() {
        //Empty
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValidationKey() {
        return validationKey;
    }

    public void setValidationKey(String validationKey) {
        this.validationKey = validationKey;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
