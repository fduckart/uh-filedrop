package edu.hawaii.its.filedrop.type;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ADMINISTRATOR;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import edu.hawaii.its.filedrop.type.position.Position;

@Entity
@Table(name = "system_role")
public class Administrator extends Position implements Serializable {

    private static final long serialVersionUID = 61L;

    // Constructor.
    public Administrator() {
        this.roleId = ADMINISTRATOR.value();
    }

    // Constructor.
    public Administrator(Integer id) {
        this();
        this.id = id;
    }

    // Constructor.
    public Administrator(String uhUuid) {
        this();
        setUhUuid(uhUuid);
    }

    // Constructor.
    public Administrator(Administrator administrator) {
        this();
        this.id = administrator.id;
        this.officeId = administrator.officeId;
        this.office = administrator.office;
        this.personId = administrator.personId;
        this.person = administrator.person;
        this.roleId = administrator.roleId;
        this.role = administrator.role;
        this.uhUuid = administrator.uhUuid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((office == null) ? 0 : office.hashCode());
        result = prime * result + ((officeId == null) ? 0 : officeId.hashCode());
        result = prime * result + ((person == null) ? 0 : person.hashCode());
        result = prime * result + ((personId == null) ? 0 : personId.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
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
        Administrator other = (Administrator) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (office == null) {
            if (other.office != null)
                return false;
        } else if (!office.equals(other.office))
            return false;
        if (officeId == null) {
            if (other.officeId != null)
                return false;
        } else if (!officeId.equals(other.officeId))
            return false;
        if (person == null) {
            if (other.person != null)
                return false;
        } else if (!person.equals(other.person))
            return false;
        if (personId == null) {
            if (other.personId != null)
                return false;
        } else if (!personId.equals(other.personId))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (roleId == null) {
            if (other.roleId != null)
                return false;
        } else if (!roleId.equals(other.roleId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Administrator ["
                + "id=" + String.format("%3s", id)
                + ", officeId=" + officeId
                + ", office=" + office
                + ", personId=" + personId
                + ", person=" + person
                + ", roleId=" + roleId
                + ", role=" + role
                + "]";
    }

}
