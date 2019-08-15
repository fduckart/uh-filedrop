package edu.hawaii.its.filedrop.type;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "fileset")
public class FileSet implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "filedrop_id", nullable = false)
    private FileDrop fileDrop;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "comment", nullable = false)
    private String comment;

    public FileSet() {

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}