package edu.hawaii.its.filedrop.type;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(FileDrop.class)
public class FileDrop_ {

    public static volatile SingularAttribute<FileDrop, Integer> id;
    public static volatile SingularAttribute<FileDrop, String> downloadKey;
    public static volatile SingularAttribute<FileDrop, String> uploadKey;
    public static volatile SingularAttribute<FileDrop, String> encryptionKey;
    public static volatile SetAttribute<FileDrop, FileSet> fileSets;

}
