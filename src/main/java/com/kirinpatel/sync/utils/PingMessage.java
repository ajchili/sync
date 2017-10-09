package com.kirinpatel.sync.utils;

public class PingMessage extends Message {

    public class Builder extends Message.Builder {

        @Override
        Message build() {
            return new PingMessage(this);
        }
    }

    public PingMessage(Builder builder) {
        body = builder.body;
    }

    @Override
    public String toString() {
        return body + "ms";
    }

    @Override
    public String getType() {
        return "ping";
    }
}
