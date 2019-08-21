package edu.hawaii.its.filedrop.type;

import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

    @Column(name = "uploader_fullname", nullable = false)
    private String uploaderFullName;

    @Column(name = "created", nullable = false)
    private Date created;

    @Column(name = "upload_key", nullable = false, unique = true)
    private String uploadKey;

    @Column(name = "download_key", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String downloadKey;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "encrypt_key", nullable = false)
    private String encryptionKey;

    @Column(name = "valid_until", nullable = false)
    private Date expiration;

    //Need to alter table from character to boolean
    @Column(name = "is_valid", nullable = false)
    private Boolean valid;

    //Need to alter table from character to boolean
    @Column(name = "require_auth", nullable = false)
    private Boolean authenticationRequired;

    @OneToMany(mappedBy = "fileDrop", cascade = CascadeType.ALL)
    private Set<FileSet> fileSet;

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

    public Set<FileSet> getFileSet() {
        return fileSet;
    }

    public void setFileSet(Set<FileSet> fileSet) {
        this.fileSet = fileSet;
    }
}
