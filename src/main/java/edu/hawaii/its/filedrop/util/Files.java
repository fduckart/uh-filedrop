package edu.hawaii.its.filedrop.util;

import org.apache.commons.io.FileUtils;

public class Files {

    // Constructor
    private Files() {
        // empty
    }

    public static String byteCountToDisplaySize(long size) {
        return FileUtils.byteCountToDisplaySize(size);
    }

}
