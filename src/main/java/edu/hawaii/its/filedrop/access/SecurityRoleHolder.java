package edu.hawaii.its.filedrop.access;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import edu.hawaii.its.filedrop.type.Role.SecurityRole;

public class SecurityRoleHolder {

    private Set<GrantedAuthority> authorities = new LinkedHashSet<>();

    // Constructor.
    public SecurityRoleHolder() {
        this(null);
    }

    // Constructor.
    public SecurityRoleHolder(Set<SecurityRole> roles) {
        if (roles != null) {
            for (SecurityRole role : roles) {
                add(role);
            }
        }
    }

    public void add(SecurityRole role) {
        authorities.add(new SimpleGrantedAuthority(role.longName()));
    }

    public Set<GrantedAuthority> getAuthorites() {
        return authorities;
    }

    public int size() {
        return authorities.size();
    }

    public boolean contains(SecurityRole role) {
        return authorities.contains(new SimpleGrantedAuthority(role.longName()));
    }

    @Override
    public String toString() {
        return "SecurityRoleHolder [authorities=" + authorities + "]";
    }
}
