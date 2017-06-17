package com.kirinpatel.util;

import java.util.Random;

/**
 * @author Kirin Patel
 * @version 0.0.1
 * @date 6/17/17
 */
public class User {

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
