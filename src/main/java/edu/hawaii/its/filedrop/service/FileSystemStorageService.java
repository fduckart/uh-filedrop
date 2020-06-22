package edu.hawaii.its.filedrop.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.hawaii.its.filedrop.configuration.StorageProperties;
import edu.hawaii.its.filedrop.exception.StorageException;
import edu.hawaii.its.filedrop.exception.StorageFileNotFoundException;

@Service
public class FileSystemStorageService implements StorageService {

    private final Log logger = LogFactory.getLog(FileSystemStorageService.class);

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public Path getRootLocation() {
        return rootLocation;
    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            Path file = rootLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if(!resource.exists()) {
                String msg = "Path does not exist: " + file;
                throw new StorageFileNotFoundException(msg);
            }

            return resource;
        } catch(Exception e) {
            throw new StorageFileNotFoundException("Error reading file: " + fileName, e);
        }
    }

    public void createDirectories(String directory) {
        if(!exists(directory)) {
            try {
                Path dirPath = rootLocation.resolve(directory);
                Files.createDirectories(dirPath);
            } catch(Exception e) {
                String msg = "Unable to create directory " + directory;
                throw new StorageException(msg, e);
            }
        }
    }

    @Override
    public void store(Resource resource) {
        store(resource, Paths.get(""));
    }

    @Override
    public String store(Resource resource, Path parent) {
        Path resolvedPath = null;
        try {
            if(resource.contentLength() == 0L) {
                String msg = "Failed to store empty file " + resource.getFilename();
                throw new StorageException(msg);
            }

            String parentDir = parent.toString();
            Path path = Paths.get(parentDir, resource.getFilename());
            resolvedPath = rootLocation.resolve(path);

            createDirectories(parentDir);
            Files.copy(resource.getInputStream(), resolvedPath);

        } catch(Exception e) {
            throw new StorageException(e.toString(), e);
        }

        return resolvedPath.toString();
    }

    @Override
    public boolean storeFileSet(Resource resource, Path filePath) {
        boolean successful = false;

        try {
            String newFilename = filePath.toString();
            Path path = rootLocation.resolve(newFilename);
            Path parent = path.getParent();

            if(parent != null) {
                Path absolutePath = parent.toAbsolutePath();
                createDirectories(absolutePath.toString());
            }

            Files.copy(resource.getInputStream(), path);
            successful = true;

        } catch(Exception e) {
            throw new StorageException(e.toString(), e);
        }

        return successful;
    }

    @Override
    public void delete(String fileName, String directory) {
        delete(Paths.get(directory, fileName));
    }

    @Override
    public void delete(Path path) {
        try {
            if(!Files.deleteIfExists(path)) {
                logger.warn("No file to delete: " + path.toString());
            }
        } catch(Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public boolean exists(String fileName, String downloadKey) {
        return exists(downloadKey + File.separator + fileName);
    }

    @Override
    public boolean exists(Resource resource, String downloadKey) {
        return exists(resource.getFilename(), downloadKey);
    }

    @Override
    public boolean exists(Path path) {
        return exists(path.toString());
    }

    @Override
    public boolean exists(String path) {
        return path != null && Files.exists(rootLocation.resolve(path));
    }

    @Override
    public String toString() {
        return "FileSystemStorageService [rootLocation=" + rootLocation + "]";
    }
}
