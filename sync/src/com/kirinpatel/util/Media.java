package com.kirinpatel.util;

import java.io.File;

public class Media {

    private String url;
    private File file;
    private long currentTime = -1;
    private long length = -1;
    private boolean isPaused;

    public Media(String url) {
        this.url = url;
    }

    public Media(File file) {
        this.file = file;
        this.url = "_" + file.getName();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void File(File file) {
        this.file = file;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public String getUrl() {
        return url;
    }

    public File getFile() {
        return file;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getLength() {
        return length;
    }

    public boolean isPaused() {
        return isPaused;
    }
}
