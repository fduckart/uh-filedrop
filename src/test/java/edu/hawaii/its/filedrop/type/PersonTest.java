package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonTest {

    private Person person;

    @BeforeEach
    public void setUp() {
        person = new Person();
    }

    @Test
    public void construction() {
        assertNotNull(person);
    }

    @Test
    public void accessors() {
        assertNotNull(person);
        assertNull(person.getId());

        person.setId(666);
        person.setName("The Beast");
        assertThat(person.getId(), equalTo(666));
        assertThat(person.getName(), equalTo("The Beast"));
        assertThat(person.getCn(), equalTo("The Beast"));
    }

    @Test
    public void testEquals() {
        Person c0 = new Person();
        assertThat(c0, equalTo(c0));
        assertThat(c0, not(equalTo(new String())));
        assertFalse(c0.equals(null));
        Person c1 = new Person();
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));

        c0.setId(1);
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        c1.setId(1);
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
    }

    @Test
    public void testHashCode() {
        Person c0 = new Person();
        Person c1 = new Person();

        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setId(1);
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setId(c0.getId());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setEmail("email");
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setEmail(c0.getEmail());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setName("name");
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setName(c0.getName());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setUhUuid("uhUuid");
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setUhUuid(c0.getUhUuid());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setUsername("username");
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setUsername(c0.getUsername());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));
    }

    @Test
    public void testToString() {
        assertThat(person.toString(), containsString("id=null, uhUuid=null"));

        person.setId(12345);
        assertThat(person.toString(), containsString("Person [id=12345,"));
    }
}
