package com.kirinpatel.sync.net;

import uk.co.caprica.vlcj.player.Equalizer;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

import java.awt.*;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Media {

    private MediaPlayer mediaPlayer;
    MediaData data = new MediaData();

    public static class MediaData implements Serializable {
        String url = "";
        String filePath;
        public long currentTime = -1;
        long length = -1;
        float rate = 1.0f;
        public boolean isPaused = false;
        public String getFormattedTime() {
            return formatTime(currentTime);
        }

        void setURL(String url) {
            this.url = url;
            this.filePath = "null";
        }

        String getURL() {
            return this.url;
        }

        public void setCurrentTime(long time) {
            this.currentTime = time;
        }

    }

    public Media(String url) {
        setURL(url);
    }

    public Media(Path filePath) {
        setFilePath(filePath);
    }

    public void initPlayer(MediaPlayerEventListener e, MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        this.mediaPlayer.setStandardMediaOptions();
        this.mediaPlayer.setPlaySubItems(true);
        this.mediaPlayer.addMediaPlayerEventListener(e);

    }

    public void setEqualizer(Equalizer equalizer) {
        mediaPlayer.setEqualizer(equalizer);
    }

    public void play() {
        if (!getURL().isEmpty() && isPaused()) {
            setPaused(false);
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (!getURL().isEmpty() && !isPaused()) {
                setPaused(true);
                mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void setTime(long time) {
        setCurrentTime(time);
        mediaPlayer.setTime(time);
    }

    public void prepareMedia() {
        if (!getFilePath().equals("null")) {
            setFilePath(Paths.get(getFilePath()));
            mediaPlayer.prepareMedia(getFilePath());
        } else {
            mediaPlayer.prepareMedia(getURL());
        }
    }

    public void parseMedia() {
        mediaPlayer.parseMedia();
    }

    public void setVolume(int volume) {
        mediaPlayer.setVolume(volume);
    }

    public void initControls() {
        mediaPlayer.setMarqueeSize(60);
        mediaPlayer.setMarqueeOpacity(200);
        mediaPlayer.setMarqueeColour(Color.white);
        mediaPlayer.setMarqueeTimeout(3500);
        mediaPlayer.setMarqueeLocation(50, 1000);
    }

    public void setURL(String url) {
        data.url = url;
        data.filePath = "null";
    }

    public void setFilePath(Path filePath) {
        data.url = "http://" + Server.ipAddress + ":8080/" + encode(filePath.getFileName().toString());
        data.filePath = filePath.toAbsolutePath().toString();
    }

    public void setCurrentTime(long currentTime) {
        data.currentTime = currentTime;
    }

    public void setLength(long length) {
        data.length = length;
    }

    public void setRate(float rate) {
        data.rate = rate >= 0.75f ? rate : 0.75f;
        mediaPlayer.setRate(data.rate);
    }

    public void setPaused(boolean isPaused) {
        data.isPaused = isPaused;
    }

    public String getURL() {
        return data.url;
    }

    public String getFilePath() {
        return data.filePath;
    }

    public long getCurrentTime() {
        return data.currentTime;
    }

    public long getLength() {
        return data.length;
    }

    public float getRate() {
        return data.rate;
    }

    public boolean isPaused() {
        return data.isPaused;
    }

    public String getFormattedTime() {
        return formatTime(data.currentTime);
    }

    public String getFormattedLength() {
        return formatTime(data.length);
    }

    /**
     * Credit: https://github.com/caprica/vlcj-player/blob/master/src/main/java/uk/co/caprica/vlcjplayer/time/Time.java
     *
     * @param value Time
     * @return Time in displayable string format
     */
    private static String formatTime(long value) {
        value /= 1000;
        int hours = (int) value / 3600;
        int remainder = (int) value - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int seconds = remainder;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    /**
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
