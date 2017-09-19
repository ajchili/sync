package com.kirinpatel.sync.utils;

import java.io.Serializable;

public class Message implements Serializable {

    private final MESSAGE_TYPE type;
    private final Object body;

    public enum MESSAGE_TYPE {

        PING(-1),
        DISCONNECTING(0),
        CONNECTING(1),
        CONNECTED(2),
        CLOSING(3),
        CLIENT_NAME(10),
        CONNECTED_CLIENTS(11),
        MEDIA_URL(20),
        MEDIA_TIME(21),
        MEDIA_RATE(22),
        MEDIA_STATE(23),
        MESSAGES(30);

        private int messageType;

        MESSAGE_TYPE(int messageType) {
            this.messageType = messageType;
        }

        public int getMessageType() {
            return messageType;
        }
    }

    public static class Builder {

        private MESSAGE_TYPE type;
        private Object body = null;

        public Builder(MESSAGE_TYPE type) {
            this.type = type;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }

    private Message(Builder builder) {
        type = builder.type;
        body = builder.body;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + body.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message))
            return false;
        else {
            Message objM = (Message) o;
            return (type == objM.getType()) && (body.equals(objM.getBody()));
        }
    }

    public MESSAGE_TYPE getType() {
        return type;
    }

    public Object getBody() {
        return body;
    }
}
