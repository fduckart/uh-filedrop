package edu.hawaii.its.filedrop.type;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.hawaii.its.filedrop.access.Authority;

@Entity
@Table(name = "role")
public class Role implements Serializable {

    private static final long serialVersionUID = 63L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role", nullable = false)
    @Access(AccessType.PROPERTY)
    private String role;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "security_role", nullable = false)
    private String securityRole;

    @Transient
    private String group;

    // Constructor.
    public Role() {
        // Empty.
    }

    // Constructor.
    public Role(Integer id) {
        this();
        this.id = id;
    }

    // Constructor.
    public Role(Integer id, SecurityRole securityRole) {
        this(id);
        setSecurityRole(securityRole.name());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        if (role != null) {
            int idx = role.indexOf('_');
            if (idx > 0) {
                group = role.substring(0, idx);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecurityRole() {
        return securityRole != null ? securityRole : "";
    }

    public void setSecurityRole(String securityRole) {
        this.securityRole = securityRole;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((securityRole == null) ? 0 : securityRole.hashCode());
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
        Role other = (Role) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (securityRole == null) {
            if (other.securityRole != null)
                return false;
        } else if (!securityRole.equals(other.securityRole))
            return false;
        return true;
    }

    public int compareTo(Role anotherRole) {
        return this.id.compareTo(anotherRole.id);
    }

    @Override
    public String toString() {
        return "Role [id=" + id
                + ", role=" + role
                + ", description=" + description
                + ", securityRole=" + securityRole
                + ", group=" + group
                + "]";
    }

    @Transient
    @JsonIgnore
    public boolean isNonUH() {
        return SecurityRole.NON_UH.name().equals(securityRole);
    }

    @Transient
    @JsonIgnore
    public boolean isSuperuser() {
        return Role.SecurityRole.SUPERUSER.name().equals(securityRole);
    }

    @Transient
    @JsonIgnore
    public boolean isAdministrator() {
        return Role.SecurityRole.ADMINISTRATOR.name().equals(securityRole)
                || Role.SecurityRole.SUPERUSER.name().equals(securityRole);
    }

    @Transient
    public String getGroup() {
        return group != null ? group : "";
    }

    // ------------------------------------------------------------------------

    public enum SecurityRole implements Authority {

        ANONYMOUS(0),
        NON_UH(1),
        ADMINISTRATOR(13),
        SUPERUSER(14),
        UH(99);

        private final int value;

        // Private constructor.
        SecurityRole(int value) {
            this.value = value;
        }

        @Override
        public String longName() {
            return "ROLE_" + name();
        }

        @Override
        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return longName();
        }

        public static SecurityRole find(String name) {
            for (SecurityRole role : SecurityRole.values()) {
                if (role.name().equals(name)) {
                    return role; // Found it.
                }
            }
            return null;
        }
    }
}
