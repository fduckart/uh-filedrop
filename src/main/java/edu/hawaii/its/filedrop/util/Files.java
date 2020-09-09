package edu.hawaii.its.filedrop.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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

    public static void deleteDirectory(Path path) throws IOException {
        File dir = path.toFile();
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    deleteDirectory(file.toPath());
                }
            }
            dir.delete();
        }
    }

}
