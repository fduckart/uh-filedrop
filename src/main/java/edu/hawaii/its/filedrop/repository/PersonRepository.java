package edu.hawaii.its.filedrop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Person;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Override
    List<Person> findAll();

    Person findById(Integer id);

    Person findByEmail(String email);

    Person findByUhUuid(String uhUuid);

}
