package com.kirinpatel.util;

import java.io.Serializable;
import java.util.Random;

/**
 * Simple object that holds all user information in one place.
 */
public class User implements Serializable {

    private String username;
    private long userID;
    private long time = -1;

    public User(String username) {
        this.username = username;
        this.userID = Math.abs(new Random().nextLong());
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

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUserID() {
        return userID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
