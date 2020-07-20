package edu.hawaii.its.filedrop.type;

import java.time.LocalDateTime;
import java.util.List;

public class FileDropInfo {
    private String uploader;
    private LocalDateTime created;
    private LocalDateTime expiration;
    private Integer downloads;
    private Integer fileDropId;
    private List<FileInfo> fileInfoList;
    private boolean valid;
    private String downloadKey;
    private List<String> recipients;

    // Constructor.
    public FileDropInfo() {
        // Empty.
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public Integer getDownloads() {
        return downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    public Integer getFileDropId() {
        return fileDropId;
    }

    public void setFileDropId(Integer fileDropId) {
        this.fileDropId = fileDropId;
    }

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getDownloadKey() {
        return downloadKey;
    }

    public void setDownloadKey(String downloadKey) {
        this.downloadKey = downloadKey;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public static class FileInfo {
        private String fileName;
        private String fileType;
        private long fileSize;
        private int downloads;

        // Constructor.
        public FileInfo() {
            // Empty.
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public int getDownloads() {
            return downloads;
        }

        public void setDownloads(int downloads) {
            this.downloads = downloads;
        }
    }
}
