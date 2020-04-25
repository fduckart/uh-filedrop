package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Validation;

public interface ValidationRepository extends JpaRepository<Validation, Integer> {
    Validation findByValidationKey(String validationKey);
}
