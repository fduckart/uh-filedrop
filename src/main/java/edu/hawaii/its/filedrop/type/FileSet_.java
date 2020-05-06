package edu.hawaii.its.filedrop.type;

import javax.persistence.metamodel.SingularAttribute;

public class FileSet_ {

    public static volatile SingularAttribute<FileSet, Integer> id;
    public static volatile SingularAttribute<FileSet, FileDrop> fileDrop;

    // Private contructor to prevent instantiation.
    private FileSet_() {
        // Empty.
    }

}
