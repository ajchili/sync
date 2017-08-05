package com.kirinpatel.sync.net;

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
        setURL(url);
    }

    public Media(Path filePath) {
        setFilePath(filePath);
    }

    public void setURL(String url) {
        this.url = url;
        this.filePath = "null";
    }

    public void setFilePath(Path filePath) {
        this.url = "http://" + Server.ipAddress + ":8080/" + encode(filePath.getFileName().toString());
        this.filePath = filePath.toAbsolutePath().toString();
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


    /**
     * Encodes URL to be used for offline media playback.
     *
     * Credit: https://www.urlencoder.org/
     *
     * @param url Un-encoded URL
     * @return Returns encoded URL
     */
    private static String encode(String url) {
        url = url.replace("%", "%25");
        url = url.replace(" ", "%20");
        url = url.replace("\"", "%22");
        url = url.replace("-", "%2D");
        url = url.replace(".", "%2E");
        url = url.replace("<", "%3C");
        url = url.replace(">", "%3E");
        url = url.replace("\\", "%5C");
        url = url.replace("^", "%5E");
        url = url.replace("_", "%5F");
        url = url.replace("`", "%60");
        url = url.replace("{", "%7B");
        url = url.replace("|", "%7C");
        url = url.replace("}", "%7D");
        url = url.replace("~", "%7E");
        return url;
    }
}
