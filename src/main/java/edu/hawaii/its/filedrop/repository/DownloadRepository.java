package edu.hawaii.its.filedrop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Download;
import edu.hawaii.its.filedrop.type.FileDrop;

public interface DownloadRepository extends JpaRepository<Download, Integer> {

    List<Download> findAllByFileDropAndFileName(FileDrop fileDrop, String fileName);
}
