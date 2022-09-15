package edu.hawaii.its.filedrop.access;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ANONYMOUS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserTest {

    @Test
    public void construction() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        User user = new User("a", authorities);
        assertNotNull(user);

        assertEquals("a", user.getUsername());
        assertEquals("a", user.getUid());
        assertThat(user.getUhUuid(), equalTo(""));
        assertNull(user.getAttributes());

        authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority(ANONYMOUS.longName()));
        user = new User("b", "12345", authorities);

        assertEquals("b", user.getUsername());
        assertEquals("b", user.getUid());
        assertEquals("12345", user.getUhUuid());
        assertThat(user.getUhUuid(), equalTo("12345"));
        assertNull(user.getAttributes());

        user.setAttributes(new UhCasAttributes());
        assertThat(user.getName(), equalTo(""));
    }

    @Test
    public void accessors() {
        Map<Object, Object> map = new HashMap<>();
        map.put("uid", "duckart");
        map.put("uhuuid", "666666");
        map.put("cn", "Frank");
        map.put("mail", "frank@example.com");
        map.put("eduPersonAffiliation", "aff");

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        User user = new User("a", authorities);
        user.setAttributes(new UhCasAttributes(map));

        assertThat(user.getAttribute("uid"), equalTo("duckart"));
        assertThat(user.getName(), equalTo("Frank"));
        assertThat(user.toString(), containsString("uid=a,"));
        assertThat(user.toString(), containsString("uhUuid=,"));
    }

    @Test
    public void testEquals() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        User u0 = new User("user0", "u0", authorities);
        assertTrue(u0.equals(u0));
        assertFalse(u0.equals(null));
        assertFalse(u0.equals(new String()));

        assertEquals(u0, u0);
        assertNotEquals(null, u0);
        assertNotEquals("", u0);
        assertNotEquals(new Date(), u0);
        assertThat(u0, not(equalTo("")));
        assertThat(u0.hashCode(), equalTo(u0.hashCode()));

        org.springframework.security.core.userdetails.User up =
                new org.springframework.security.core.userdetails.User("user0", "u0", authorities);
        assertFalse(u0.equals(up));
        assertTrue(up.equals(u0));
        assertThat(up.hashCode(), not(equalTo(u0.hashCode())));

        User u1 = new User("user1", "u1", authorities);
        assertNotEquals(u0, u1);

        User ua = new User("user0", "u0", authorities);
        assertEquals(u0, ua);
        assertEquals(ua, u0);

        User ub = new User("user0", "ub", authorities);
        assertNotEquals(u0, ub);
        assertEquals(ua, u0);
        ub = null;

        User uc = new User("user0", "ub", authorities);
        assertNotEquals(u0, uc);
        assertNotEquals(uc, u0);
        uc = null;

        User ud = new User("user0", null, authorities);
        assertEquals(u0, ua);
        assertNotEquals(ud, u0);
        u0 = new User("user0", null, authorities);
        assertEquals(ud, u0);
        ud = null;
    }

}
