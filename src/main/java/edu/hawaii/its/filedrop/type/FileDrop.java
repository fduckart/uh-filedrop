package edu.hawaii.its.filedrop.type;


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "filedrop")
public class FileDrop {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uploader", nullable = false)
    private String uploader;

    @Column(name = "uploaderFullName", nullable = false)
    private String uploaderFullName;

    @Column(name = "created", nullable = false)
    private Date created;

    @Column(name = "upload_key", nullable = false)
    private String uploadKey;

    @Column(name = "download_key", nullable = false)
    private String downloadKey;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "encrypt_key", nullable = false)
    private String encryptionKey;

    @Column(name = "valid_until", nullable = false)
    private Date expiration;

    @Column(name = "is_valid", nullable = false)
    private Boolean valid;

    @Column(name = "require_auth", nullable = false)
    private Boolean authenticationRequired;

    public FileDrop() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getUploaderFullName() {
        return uploaderFullName;
    }

    public void setUploaderFullName(String uploaderFullName) {
        this.uploaderFullName = uploaderFullName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getUploadKey() {
        return uploadKey;
    }

    public void setUploadKey(String uploadKey) {
        this.uploadKey = uploadKey;
    }

    public String getDownloadKey() {
        return downloadKey;
    }

    public void setDownloadKey(String downloadKey) {
        this.downloadKey = downloadKey;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(Boolean authenticationRequired) {
        this.authenticationRequired = authenticationRequired;
    }
}
