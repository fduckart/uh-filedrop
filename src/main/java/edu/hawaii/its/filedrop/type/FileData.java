package edu.hawaii.its.filedrop.type;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "file_data")
public class FileData implements Serializable {

    private static final long serialVersionUID = 101L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "comment", nullable = false)
    @Access(AccessType.PROPERTY)
    private String comment = "";

    @OneToOne
    @JoinColumn(name = "fileset_id", nullable = false)
    private FileSet fileSet;

    // Constructor.
    public FileData() {
        // Empty.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FileSet getFileSet() {
        return fileSet;
    }

    public void setFileSet(FileSet fileSet) {
        this.fileSet = fileSet;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment != null ? comment : "";
    }

    @Override
    public String toString() {
        return "FileData [id=" + id
            + ", fileName=" + fileName
            + ", comment=" + comment
            + "]";
    }
}
