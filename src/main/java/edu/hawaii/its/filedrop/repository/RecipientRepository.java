package edu.hawaii.its.filedrop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.Recipient;

public interface RecipientRepository extends JpaRepository<Recipient, Integer> {

    List<Recipient> findAllByFileDrop(FileDrop fileDrop);

}
