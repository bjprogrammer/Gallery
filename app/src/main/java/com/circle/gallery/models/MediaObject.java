package com.circle.gallery.models;

public class MediaObject {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeTyoe) {
        this.mimeType = mimeTyoe;
    }

    private String dateModified;
    private String title;
    private String filePath;
    private int mediaType;
    private String mimeType;

    public MediaObject(String title, String dateModified, String filePath, int mediaType, String mimeType) {
        this.title = title;
        this.dateModified = dateModified;
        this.filePath = filePath;
        this.mediaType = mediaType;
        this.mimeType = mimeType;
    }
}










