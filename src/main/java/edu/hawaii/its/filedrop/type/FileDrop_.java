package edu.hawaii.its.filedrop.type;

import java.time.LocalDateTime;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(FileDrop.class)
public class FileDrop_ {

    public static volatile SingularAttribute<FileDrop, Integer> id;
    public static volatile SingularAttribute<FileDrop, Boolean> authenticationRequired;
    public static volatile SingularAttribute<FileDrop, LocalDateTime> created;
    public static volatile SingularAttribute<FileDrop, String> downloadKey;
    public static volatile SingularAttribute<FileDrop, String> uploadKey;
    public static volatile SingularAttribute<FileDrop, String> encryptionKey;
    public static volatile SingularAttribute<FileDrop, LocalDateTime> expiration;
    public static volatile SetAttribute<FileDrop, FileSet> fileSet;
    public static volatile ListAttribute<FileDrop, Recipient> recipients;
    public static volatile SingularAttribute<FileDrop, String> uploaderFullName;
    public static volatile SingularAttribute<FileDrop, String> uploader;
    public static volatile SingularAttribute<FileDrop, Boolean> valid;

    // Private contructor to prevent instantiation.
    private FileDrop_() {
        // Empty.
    }

}
