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

public class AdministratorTest {

    private Administrator administrator;

    @BeforeEach
    public void setUp() {
        administrator = new Administrator();
    }

    @Test
    public void accessors() {
        assertNull(administrator.getId());
        assertNull(administrator.getOffice());
        assertNull(administrator.getPerson());
        assertNull(administrator.getRole());

        administrator.setId(666);
        assertThat(administrator.getId(), equalTo(666));
        administrator.setOffice(new Office());
        administrator.setPerson(new Person());
        assertNotNull(administrator.getId());
        assertNotNull(administrator.getOffice());
        assertNotNull(administrator.getPerson());

        administrator.setRole(new Role(1));
        assertNotNull(administrator.getRole());
        assertThat(administrator.getRoleId(), equalTo(1));
        administrator.setRoleId(2);
        assertThat(administrator.getRoleId(), equalTo(2));

        administrator.setUhUuid(null);
        assertThat(administrator.getUhUuid(), equalTo(""));
        assertThat(administrator.isValid(), equalTo(true));

        administrator.setUhUuid("");
        assertThat(administrator.getUhUuid(), equalTo(""));
        assertThat(administrator.isValid(), equalTo(true));

        administrator.setUhUuid("top");
        assertThat(administrator.getUhUuid(), equalTo("top"));
        assertThat(administrator.isValid(), equalTo(true));
    }

    @Test
    public void constructors() {
        administrator.setId(1);
        administrator.setOfficeId(2);

        Administrator c0 = new Administrator(administrator);
        assertThat(c0.getId(), equalTo(1));
        assertThat(c0.getOfficeId(), equalTo(2));

        Office office = new Office(123);
        Person person = new Person(456);
        Administrator c1 = new Administrator();
        c1.setOffice(office);
        c1.setPerson(person);
        assertNotNull(c1.getOffice());
        assertNotNull(c1.getPerson());
        assertNull(c1.getPersonId()); // ?? Hmmm.
        assertNull(c1.getOfficeId()); // ?? Hmmm.

        Administrator c2 = new Administrator(123);
        assertThat(c2.getId(), equalTo(123));

        Administrator c3 = new Administrator("ABC");
        assertThat(c3.getUhUuid(), equalTo("ABC"));
        assertThat(c3.isValid(), equalTo(false));
    }

    @Test
    public void testEquals() {
        Administrator c0 = new Administrator();
        assertThat(c0, equalTo(c0));
        assertThat(c0, not(equalTo(new String())));
        assertFalse(c0.equals(null));
        Administrator c1 = new Administrator();
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
        Administrator c0 = new Administrator();
        Administrator c1 = new Administrator();

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

        c0.setOffice(new Office());
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setOffice(c0.getOffice());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setOfficeId(9);
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setOfficeId(c0.getOfficeId());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setPerson(new Person());
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setPerson(c0.getPerson());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setPersonId(9);
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setPersonId(c0.getPersonId());
        assertThat(c0, equalTo(c1));
        assertThat(c1, equalTo(c0));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setRole(new Role());
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setRole(c0.getRole());
        assertThat(c0, equalTo(c1));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));

        c0.setRoleId(9);
        assertThat(c0, not(equalTo(c1)));
        assertThat(c1, not(equalTo(c0)));
        assertThat(c0.hashCode(), not(equalTo(c1.hashCode())));
        assertThat(c1.hashCode(), not(equalTo(c0.hashCode())));
        c1.setRoleId(c0.getRoleId());
        assertThat(c0, equalTo(c1));
        assertThat(c0.hashCode(), equalTo(c1.hashCode()));
        assertThat(c1.hashCode(), equalTo(c0.hashCode()));
    }

    @Test
    public void testToString() {
        assertThat(administrator.toString(), containsString("officeId=null, office=null"));

        administrator.setOfficeId(9876);
        assertThat(administrator.toString(), containsString("officeId=9876, office=null"));

        assertThat(administrator.toString(), containsString("Administrator [id=null,"));
        administrator.setId(1);
        assertThat(administrator.toString(), containsString("Administrator [id=  1,"));
        administrator.setId(12);
        assertThat(administrator.toString(), containsString("Administrator [id= 12,"));
        administrator.setId(123);
        assertThat(administrator.toString(), containsString("Administrator [id=123,"));
        administrator.setId(1234);
        assertThat(administrator.toString(), containsString("Administrator [id=1234,"));
    }
}
