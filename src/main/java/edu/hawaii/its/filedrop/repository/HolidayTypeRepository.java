package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hawaii.its.filedrop.type.Type;

@Repository
public interface HolidayTypeRepository extends JpaRepository<Type, Integer> {

    Type findById(Integer id);

}
