package edu.hawaii.its.filedrop.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public interface StorageService {

    Path getRootLocation();

    Resource loadAsResource(String fileName);

    void store(Resource resource);

    String store(Resource resource, Path parent);

    Resource storeFileSet(Resource resource, Path filePath);

    void delete(String fileName, String directory);

    void delete(Path path);

    boolean exists(String fileName, String downloadKey);

    boolean exists(Resource resource, String downloadKey);

    boolean exists(Path path);

    boolean exists(String path);
}
