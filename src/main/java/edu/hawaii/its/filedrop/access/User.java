package edu.hawaii.its.filedrop.access;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import edu.hawaii.its.filedrop.type.Role.SecurityRole;

public class User extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 5L;
    private String uhUuid;
    private UhAttributes attributes;

    // Constructor.
    public User(String username, String uhUuid, Collection<GrantedAuthority> authorities) {
        super(username, "", authorities);
        setUhUuid(uhUuid);
    }

    // Constructor.
    public User(String username, Collection<GrantedAuthority> authorities) {
        this(username, null, authorities);
    }

    public String getUid() {
        return getUsername();
    }

    public String getUhUuid() {
        return uhUuid;
    }

    private void setUhUuid(String uhUuid) {
        this.uhUuid = uhUuid != null ? uhUuid : "";
    }

    public String getAttribute(String name) {
        return attributes.getValue(name);
    }

    public UhAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(UhAttributes attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return attributes.getValue("cn");
    }

    public boolean hasRole(SecurityRole role) {
        return getAuthorities().contains(new SimpleGrantedAuthority(role.longName()));
    }

    public boolean hasRole(String roleLongName) {
        if (roleLongName != null) {
            if (!roleLongName.startsWith("ROLE_")) {
                roleLongName = "ROLE_" + roleLongName;
            }
            return getAuthorities().contains(new SimpleGrantedAuthority(roleLongName));
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + uhUuid.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (!uhUuid.equals(other.uhUuid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [uid=" + getUid()
                + ", uhUuid=" + uhUuid
                + ", super-class: " + super.toString() + "]";
    }

}
