package edu.hawaii.its.filedrop.type;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "fileset")
public class FileSet {

    @JoinColumn(name = "filedrop_id", nullable = false)
    @ManyToOne
    private FileDrop fileDrop;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "comment", nullable = false)
    private String comment;

    public FileSet() {

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
