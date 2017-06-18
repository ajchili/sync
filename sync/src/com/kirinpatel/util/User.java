package com.kirinpatel.util;

import java.io.Serializable;
import java.util.Random;

/**
 * @author Kirin Patel
 * @version 0.0.2
 * @date 6/17/17
 */
public class User implements Serializable {

    private String username;
    private long userID;

    public User(String username) {
        this.username = username;
        this.userID = new Random().nextLong();
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
            return username.equals(objU.getUsername()) && userID == objU.getUserID();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public long getUserID() {
        return userID;
    }
}
