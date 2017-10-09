package com.kirinpatel.sync.utils;

import java.util.ArrayList;

public class MessagesMessage extends Message {

    public class Builder extends Message.Builder {

        @Override
        Message build() {
            return new MessagesMessage(this);
        }
    }

    public MessagesMessage(Builder builder) {
        body = builder.body;
    }

    @Override
    public String toString() {
        String messages = "[ ";
        for (User message : (ArrayList<User>) body) {
            messages += message.getUsername() + ", ";
        }
        messages += " ]";
        return "Messages: " + messages;
    }

    @Override
    public String getType() {
        return "messagesMessage";
    }
}
