package edu.hawaii.its.filedrop.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.repository.PersonRepository;
import edu.hawaii.its.filedrop.type.Person;
import edu.hawaii.its.filedrop.type.PersonIdentifiable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void findAll() {
        assertNotNull(personService);

        long count = personRepository.count();
        assertThat(count, equalTo(7L));

        final Integer id = 1;
        Person p0 = personService.findById(id);
        Person p1 = personService.findById(id);
        assertEquals(p0, p1);
        assertSame(p0, p1);

        assertEquals(p1, p0);
        assertSame(p1, p0);

        Person p2 = personService.findById(id);
        assertEquals(p0, p2);
        assertSame(p0, p2);

        Person pn = personService.findById(666);
        assertNull(pn);
    }

    @Test
    public void findPersonByEmail() {
        Person person = personService.findPersonByEmail("nonone@example.com");
        assertFalse(person.isValid());

        String email = "duckart@hawaii.edu";
        person = personService.findPersonByEmail(email);
        assertNotNull(person);
        assertThat(person.getEmail(), equalTo(email));
        assertThat(person.getName(), equalTo("Frank Duckart"));
    }

    @Test
    public void findById() {
        Integer id = 2;
        Person p0 = personService.findById(id);
        assertThat(p0.getName(), equalTo("Frank Duckart"));

        // See if the caching is working.
        Person p1 = personService.findById(id);
        assertEquals(p0, p1);
        assertSame(p0, p1);
    }

    @Test
    public void evictPersonCaches() {
        Integer id = 2;
        Person p0 = personService.findById(id);
        assertThat(p0.getName(), equalTo("Frank Duckart"));

        // See if the caching is working.
        Person p1 = personService.findById(id);
        assertEquals(p0, p1);
        assertSame(p0, p1);

        personService.evictPersonCaches();

        Person p2 = personService.findById(id);
        assertEquals(p0, p2);
        assertEquals(p0, p1);
        assertEquals(p2, p0);
        assertEquals(p2, p1);
        assertNotSame(p0, p2);
    }

    @Test
    public void addPerson() {
        long count0 = personService.findAll().size();
        assertThat(personService.findPersons().size(), equalTo((int) count0));

        String uhUuid = "20000004";
        Person person = personService.findPerson(uhUuid);
        assertFalse(person.isValid());

        // What we are testing.
        person = personService.addPerson(uhUuid);

        assertTrue(person.isValid());

        long count1 = personService.findAll().size();
        assertThat(count1, equalTo(count0 + 1));
        assertThat(personService.findPersons().size(), equalTo((int) count1));

        // Clean up.
        personService.delete(person);

        long count2 = personService.findAll().size();
        assertThat(count2, equalTo(count0));
        assertThat(personService.findPersons().size(), equalTo((int) count2));
    }

    @Test
    public void findPerson() {
        Person p0 = personService.findPerson("non-existent");
        assertNull(p0.getId());
        assertThat(p0.getUhUuid(), equalTo(""));
        assertThat(p0.getName(), equalTo(""));

        Person p1 = personService.findPerson("17958670");
        assertThat(p1.getId(), equalTo(2));
        assertThat(p1.getUhUuid(), equalTo("17958670"));
        assertThat(p1.getName(), equalTo("Frank Duckart"));

        // Check the interface designed call.
        Person p2 = personService.findPerson(p1);
        assertThat(p2, equalTo(p1));

        PersonIdentifiable pii = new PersonIdentifiable() {
            @Override
            public String getUhUuid() {
                return PersonIdentifiable.super.getUhUuid();
            }
        };
        p2 = personService.findPerson(pii);
        assertThat(p2, not(equalTo(p1)));

    }

    @Test
    public void findPersons() {
        List<Person> persons = personService.findPersons();
        assertTrue(persons.size() > 0);
    }

    @Test
    public void findAdministrators() {
        assertTrue(personService.findAdministrators().size() > 0);
    }

}
