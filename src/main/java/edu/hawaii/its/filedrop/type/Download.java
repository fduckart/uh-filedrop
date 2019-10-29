package edu.hawaii.its.filedrop.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;

@Entity
@Table(name = "download")
public class Download {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "filedrop_id")
    private FileDrop fileDrop;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "status")
    private Status status;

    @Column(name = "started")
    private DateTime started;

    @Column(name = "completed")
    private DateTime completed;

    @Column(name = "ip_addr", length = 16)
    private String ipAddress;

    //Constructor
    public Download() {
        //Empty
    }

    enum Status {
        INPROGRESS,
        COMPLETED,
        CANCELED;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FileDrop getFileDrop() {
        return fileDrop;
    }

    public void setFileDrop(FileDrop fileDrop) {
        this.fileDrop = fileDrop;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public DateTime getStarted() {
        return started;
    }

    public void setStarted(DateTime started) {
        this.started = started;
    }

    public DateTime getCompleted() {
        return completed;
    }

    public void setCompleted(DateTime completed) {
        this.completed = completed;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
