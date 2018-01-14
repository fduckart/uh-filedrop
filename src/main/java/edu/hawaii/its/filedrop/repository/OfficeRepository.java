package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Office;

public interface OfficeRepository extends JpaRepository<Office, Integer> {

    Office findById(Integer id);
}
