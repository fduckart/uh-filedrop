package edu.hawaii.its.filedrop.type;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "office")
public class Office implements OfficeIdentifiable, Serializable {

    private static final long serialVersionUID = 49L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "campus_id", nullable = false)
    private Integer campusId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "sort_id", nullable = false)
    private Integer sortId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campus_id", updatable = false, insertable = false)
    private Campus campus;

    // Constructor.
    public Office() {
        // Empty.
    }

    // Constructor.
    public Office(Integer id) {
        this();
        this.id = id;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
        setCampusId(campus != null ? campus.getId() : null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getOfficeId() {
        return getId();
    }

    public Integer getCampusId() {
        return campusId;
    }

    public void setCampusId(Integer campusId) {
        this.campusId = campusId;
    }

    public Integer getSortId() {
        return sortId;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public String getLongDescription() {
        return campus.getDescription() + ": " + this.description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Office other = (Office) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Office [id=" + id
                + ", campusId=" + campusId
                + ", campus=" + campus
                + ", description=" + description
                + "]";
    }

}
