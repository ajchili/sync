package com.kirinpatel.util;

import java.io.Serializable;
import java.nio.file.Path;

public class Media implements Serializable {

    private String url;
    private String filePath;
    private long currentTime = -1;
    private long length = -1;
    private float rate = 1.0f;
    private boolean isPaused;

    public Media(String url) {
        this.url = url;
        this.filePath = "null";
    }

    public Media(Path filePath) {
        this.filePath = filePath.toString();
        this.url = "_" + URLEncoding.encode(filePath.getFileName().toString());
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath.toString();
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setRate(float rate) {
        this.rate = rate >= 0.75f ? rate : 0.75f;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public String getURL() {
        return url;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getLength() {
        return length;
    }

    public float getRate() {
        return rate;
    }

    public boolean isPaused() {
        return isPaused;
    }
}
