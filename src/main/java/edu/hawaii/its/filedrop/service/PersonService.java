package edu.hawaii.its.filedrop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.hawaii.its.filedrop.repository.AdministratorRepository;
import edu.hawaii.its.filedrop.repository.PersonRepository;
import edu.hawaii.its.filedrop.type.Administrator;
import edu.hawaii.its.filedrop.type.Person;
import edu.hawaii.its.filedrop.type.PersonIdentifiable;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private LdapService ldapService;

    @Transactional(readOnly = true)
    @Cacheable(value = "personByIdCache", key = "#id")
    public Person findById(Integer id) {
        return personRepository.findById(id);
    }

    public Person findPerson(PersonIdentifiable p) {
        Person person = personRepository.findByUhUuid(p.getUhUuid());
        return person != null ? person : new Person();
    }

    public Person findPerson(String uhUuid) {
        Person person = personRepository.findByUhUuid(uhUuid);
        return person != null ? person : new Person();
    }

    public Person findPersonByEmail(String email) {
        Person person = personRepository.findByEmail(email);
        return person != null ? person : new Person();
    }

    @Cacheable(value = "personCache")
    public List<Person> findPersons() {
        return personRepository.findAll();
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    private Person addPerson(Person person) {
        return personRepository.save(person);
    }

    @Transactional
    @Caching(put = @CachePut(value = "personByIdCache", key = "#result.id"),
            evict = @CacheEvict(value = "personCache", allEntries = true))
    public Person addPerson(String uhUuid) {

        Person person = findPerson(uhUuid);
        LdapPerson ldapP = ldapService.findByUhUuid(uhUuid);
        person.setUhUuid(ldapP.getUhUuid());
        person.setName(ldapP.getCn());
        person.setEmail(ldapP.getUid() + "@hawaii.edu");
        person.setUsername(ldapP.getUid());
        person = addPerson(person);

        return person;
    }

    @Caching(evict = {
            @CacheEvict(value = "personCache", allEntries = true),
            @CacheEvict(value = "personByIdCache", key = "#person.id") })
    public void delete(Person person) {
        personRepository.delete(person);
    }

    @Caching(evict = {
            @CacheEvict(value = "personCache", allEntries = true),
            @CacheEvict(value = "personByIdCache", allEntries = true) })
    public void evictPersonCaches() {
        // Empty.
    }

    @Transactional(readOnly = true)
    public List<Administrator> findAdministrators() {
        return administratorRepository.findAdministrators();
    }

}
