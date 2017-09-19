package com.kirinpatel.sync.utils;

import com.kirinpatel.sync.net.Media;

import java.io.Serializable;
import java.util.Random;

public class User implements Serializable {

    private String username;
    private long userID;
    private Media media;
    private long ping = 0;

    public User(String username) {
        this.username = username;
        this.userID = Math.abs(new Random().nextLong());
        this.media = new Media("");
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) {
            return false;
        } else {
            User objU = (User) o;
            return username.equals(objU.username) && userID == objU.userID;
        }
    }

    public String getUsername() {
        return username;
    }

    public long getUserID() {
        return userID;
    }

    public Media getMedia() {
        return media;
    }

    public long getPing() {
        return ping;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }
}
