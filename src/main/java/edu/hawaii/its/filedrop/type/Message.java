package edu.hawaii.its.filedrop.type;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "message")
public class Message implements Serializable {

    private static final long serialVersionUID = 2L;

    public static final int JUMBOTRON_MESSAGE = 1;
    public static final int GATE_MESSAGE = 2;
    public static final int UNAVAILABLE_MESSAGE = 3;

    private Integer id;
    private Integer typeId;
    private String text = "";
    private String enabled;

    // Constructor.
    public Message() {
        // Empty.
    }

    @Id
    @Column(name = "MSG_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "MSG_TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Column(name = "MSG_ENABLED", columnDefinition = "char")
    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    @Column(name = "MSG_TYPE_ID")
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    @Override
    public String toString() {
        return "Message [id=" + id
                + ", typeId=" + typeId
                + ", enabled=" + enabled
                + ", text=" + text
                + "]";
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
        Message other = (Message) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
