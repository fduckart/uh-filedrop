package edu.hawaii.its.filedrop.type;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "message")
public class Message implements Serializable {

    private static final long serialVersionUID = 2L;

    public static final int JUMBOTRON_MESSAGE = 1;
    public static final int GATE_MESSAGE = 1;
    public static final int UNAVAILABLE_MESSAGE = 3;

    @Id
    @Column(name = "MSG_ID")
    private Integer id;

    @Column(name = "MSG_TYPE_ID")
    private Integer typeId;

    @Column(name = "MSG_TEXT")
    private String text = "";

    @Column(name = "msg_enabled", nullable = false)
    @Convert(converter = BooleanToCharacterConverter.class)
    private Boolean enabled;

    // Constructor.
    public Message() {
        // Empty.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

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
