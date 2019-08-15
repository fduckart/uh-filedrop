package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.FileSet;

public interface FileSetRepository extends JpaRepository<FileSet, Integer> {
}
