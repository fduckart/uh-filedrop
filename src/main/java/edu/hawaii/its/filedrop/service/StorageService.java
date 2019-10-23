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

    void delete(String fileName, String directory);

    void delete(Path path);

    boolean exists(String fileName, String downloadKey);

    boolean exists(MultipartFile file, String downloadKey);

    boolean exists(Path path);

    boolean exists(String path);
}
