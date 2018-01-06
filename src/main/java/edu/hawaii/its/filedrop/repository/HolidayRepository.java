package edu.hawaii.its.filedrop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hawaii.its.filedrop.type.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    Holiday findById(Integer id);

    @Override
    Page<Holiday> findAll(Pageable pageable);

    List<Holiday> findAllByOrderByObservedDateDesc();

}
