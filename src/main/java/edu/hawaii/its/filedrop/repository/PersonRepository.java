package edu.hawaii.its.filedrop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Person;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Override
    List<Person> findAll();

    @Override
    Optional<Person> findById(Integer id);

    Person findByEmail(String email);

    Person findByUhUuid(String uhUuid);

}
