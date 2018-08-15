package com.kirinpatel.sync.net;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserTest {

    private User user;

    @Before
    public void setup() {
        user = new User("Testing");
    }

    @Test
    public void initializedProperly() {
        assert user.getUsername().equals("Testing");
        assert user.getMedia().equals(new Media("").data);
        assert user.getPing() == 0;
    }

    @Test
    public void setUsername() {
        user.setUsername("Testing1");
        assert user.getUsername().equals("Testing1");
    }

    @Test
    public void setMedia() {
        user.setMedia(new Media("test").data);
        assert user.getMedia().equals(new Media("test").data);
    }

    @Test
    public void setPing() {
        user.setPing(999);
        assert user.getPing() == 999;
    }
}
