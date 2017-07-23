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
    private long ping = 0;
    private boolean wantsToPause = false;

    /**
     *
     * @param username
     */
    public User(String username) {
        this.username = username;
        this.userID = Math.abs(new Random().nextLong());
    }

    /**
     *
     * @return Returns user in printable string
     */
    @Override
    public String toString() {
        return username;
    }

    /**
     *
     * @param o Object
     * @return Returns if object is equal to user
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) {
            return false;
        } else {
            User objU = (User) o;
            return username.equals(objU.username) && userID == objU.userID;
        }
    }

    /**
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return
     */
    public long getUserID() {
        return userID;
    }

    /**
     *
     * @return
     */
    public long getTime() {
        return time;
    }

    public boolean doesWantToPause() {
        return wantsToPause;
    }

    public long getPing() {
        return ping;
    }


    /**
     *
     * @param time
     */
    public void setTime(long time) {
        this.time = time;
    }

    public void setWantsToPause(boolean wantsToPause) {
        this.wantsToPause = wantsToPause;
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                setWantsToPause(false);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setPing(long ping) {
        this.ping = ping;
    }
}
