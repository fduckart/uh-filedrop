package edu.hawaii.its.filedrop.type;

import javax.persistence.metamodel.SingularAttribute;

public class FileSet_ {

    public static volatile SingularAttribute<FileSet, Integer> id;
    public static volatile SingularAttribute<FileSet, String> comment;
    public static volatile SingularAttribute<FileSet, FileDrop> fileDrop;
    public static volatile SingularAttribute<FileSet, String> fileName;
    public static volatile SingularAttribute<FileSet, Long> size;
    public static volatile SingularAttribute<FileSet, String> type;

    // Private contructor to prevent instantiation.
    private FileSet_() {
        // Empty.
    }

}
