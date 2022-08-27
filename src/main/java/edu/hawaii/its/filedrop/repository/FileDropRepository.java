package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import edu.hawaii.its.filedrop.type.FileDrop;

public interface FileDropRepository extends JpaRepository<FileDrop, Integer>, JpaSpecificationExecutor<FileDrop> {
    // Empty.
}
