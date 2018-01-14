package edu.hawaii.its.filedrop.access;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ANONYMOUS;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AnonymousUser extends User {

    private static final long serialVersionUID = 3L;

    public AnonymousUser() {
        super("anonymous", authorities());
        setAttributes(new UhCasAttributes());
    }

    private static Collection<GrantedAuthority> authorities() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ANONYMOUS.longName()));
        return authorities;
    }
}
