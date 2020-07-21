package edu.hawaii.its.filedrop.util;

import java.io.File;

import org.apache.commons.io.FileUtils;

public final class Files {

    // Constructor
    private Files() {
        // empty
    }

    public static String byteCountToDisplaySize(long size) {
        return FileUtils.byteCountToDisplaySize(size);
    }

    public static boolean exists(String path) {
        return path != null && new File(path).exists();
    }

    public static boolean isDirectory(String path) {
        return path != null && new File(path).isDirectory();
    }

    public static boolean isFile(String path) {
        return path != null && new File(path).isFile();
    }

    public static long fileSize(String path) {
        File file = new File(path);
        return file.length();
    }

}
