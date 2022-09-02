package edu.hawaii.its.filedrop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

public interface FileSetRepository extends JpaRepository<FileSet, Integer> {

    @Override
    Optional<FileSet> findById(Integer id);

    List<FileSet> findAllByFileDrop(FileDrop fileDrop);

}
