package edu.hawaii.its.filedrop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.FileData;
import edu.hawaii.its.filedrop.type.FileSet;

public interface FileDataRepository extends JpaRepository<FileData, Integer> {

    FileData findByFileSet(FileSet fileSet);

}
