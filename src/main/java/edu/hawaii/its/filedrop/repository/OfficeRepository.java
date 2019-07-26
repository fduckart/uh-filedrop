package edu.hawaii.its.filedrop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Office;

public interface OfficeRepository extends JpaRepository<Office, Integer> {

    @Override
    Optional<Office> findById(Integer id);

    List<Office> findAllByOrderBySortId();
}
