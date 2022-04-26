package edu.hawaii.its.filedrop.repository.specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileDrop_;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Recipient;

public class FileDropSpecification {

    // Private contructor to prevent instantiation.
    private FileDropSpecification() {
        // Empty.
    }

    public static Specification<FileDrop> withId(Integer id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<Integer> path = root.get(FileDrop_.id);
            Predicate predicate = criteriaBuilder.equal(path, id);
            return criteriaBuilder.and(predicate);
        };
    }

    public static Specification<FileDrop> withUploader(String uploader) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> path = root.get(FileDrop_.uploader);
            Predicate predicate = criteriaBuilder.equal(path, uploader);
            return criteriaBuilder.and(predicate);
        };
    }

    public static Specification<FileDrop> withDownloadKey(String downloadKey) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> path = root.get(FileDrop_.downloadKey);
            Predicate predicate = criteriaBuilder.equal(path, downloadKey);
            return criteriaBuilder.and(predicate);
        };
    }

    public static Specification<FileDrop> withUploadKey(String uploadKey) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> path = root.get(FileDrop_.uploadKey);
            Predicate predicate = criteriaBuilder.equal(path, uploadKey);
            return criteriaBuilder.and(predicate);
        };
    }

    public static Specification<FileDrop> withEncryptionKey(String encryptionKey) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> path = root.get(FileDrop_.encryptionKey);
            Predicate predicate = criteriaBuilder.equal(path, encryptionKey);
            return criteriaBuilder.and(predicate);
        };
    }

    public static Specification<FileDrop> withFileSetId(Integer id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<FileSet, FileDrop> fileDropFiles = root.join("fileSet");
            return criteriaBuilder.equal(fileDropFiles.get(FileDrop_.id), id);
        };
    }

    public static Specification<FileDrop> withRecipient(String recipient) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<Recipient, FileDrop> join = root.join("recipients");
            return criteriaBuilder.equal(join.get("name"), recipient);
        };
    }

    public static Specification<FileDrop> isValid(Boolean valid) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<Boolean> path = root.get(FileDrop_.valid);
            Predicate predicate = criteriaBuilder.equal(path, valid);
            return criteriaBuilder.and(predicate);
        };
    }

    public static Specification<FileDrop> isExpiring() {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<LocalDateTime> path = root.get(FileDrop_.expiration);
            Path<Boolean> path2 = root.get(FileDrop_.valid);
            Predicate predicate = criteriaBuilder.lessThanOrEqualTo(path, LocalDateTime.now());
            Predicate predicate2 = criteriaBuilder.equal(path2, true);
            return criteriaBuilder.and(predicate, predicate2);
        };
    }
}
