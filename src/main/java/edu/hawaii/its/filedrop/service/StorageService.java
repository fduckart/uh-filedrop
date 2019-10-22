package edu.hawaii.its.filedrop.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {

    Path getRootLocation();

    Resource loadAsResource(String fileName);

    void store(MultipartFile file);

    String store(MultipartFile file, Path parent);

    boolean storeFileSet(MultipartFile file, Path filePath);

    void delete(String fileName, Integer fileSetId);

    void delete(String fileName, String directory);

    void delete(Path path);

    boolean exists(String fileName, Integer fileSetId);

    boolean exists(MultipartFile file, Integer fileSetId);

    boolean exists(Path path);

    boolean exists(String path);
}
