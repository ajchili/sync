package com.kirinpatel.util;

import java.io.Serializable;
import java.util.Random;

/**
 * @author Kirin Patel
 * @version 0.0.3
 * @date 6/17/17
 */
public class User implements Serializable {

    private String username;
    private long userID;
    private boolean wantsToPause = false;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWantsToPause(boolean wantsToPause) {
        this.wantsToPause = wantsToPause;
    }

    public String getUsername() {
        return username;
    }

    public long getUserID() {
        return userID;
    }

    public boolean getWantsToPause() {
        return wantsToPause;
    }
}
