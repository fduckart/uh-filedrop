package edu.hawaii.its.filedrop.type;

import javax.persistence.metamodel.SingularAttribute;

public class FileSet_ {

    private FileSet_() {
        //Empty constructor
    }

    public static volatile SingularAttribute<FileSet, Integer> id;
    public static volatile SingularAttribute<FileSet, FileDrop> fileDrop;
}
