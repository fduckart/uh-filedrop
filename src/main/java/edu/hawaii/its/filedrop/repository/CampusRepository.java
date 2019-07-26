package edu.hawaii.its.filedrop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Campus;

public interface CampusRepository extends JpaRepository<Campus, Integer> {

    @Override
    Optional<Campus> findById(Integer id);

    List<Campus> findAllByActualOrderById(String actual);

}
