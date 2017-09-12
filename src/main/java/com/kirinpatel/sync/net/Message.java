package com.kirinpatel.sync.net;

public class Message {

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

    static class Builder {

        private MESSAGE_TYPE type;
        private Object body = null;

        Builder(MESSAGE_TYPE type) {
            this.type = type;
        }

        Builder body(Object body) {
            this.body = body;
            return this;
        }

        Message build() {
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

    MESSAGE_TYPE getType() {
        return type;
    }

    Object getBody() {
        return body;
    }
}
