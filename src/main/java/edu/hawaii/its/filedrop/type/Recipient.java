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

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "recipient")
public class Recipient implements Serializable {

    private static final long serialVersionUID = 102L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "filedrop_id", nullable = false)
    @JsonBackReference
    private FileDrop fileDrop;

    @Column(name = "name", nullable = false)
    private String name;

    //Constructor
    public Recipient() {
        //Empty
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Recipient [id=" + id
                + ", fileDropId=" + fileDrop.getId()
                + ", name=" + name
                + "]";
    }
}
