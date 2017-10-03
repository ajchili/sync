package com.kirinpatel.sync.utils;

public class PingMessage extends Message {

    @Override
    public String toString() {
        return body + "ms";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PingMessage) {
            PingMessage message = (PingMessage) o;
            return body == message.body;
        }
        return false;
    }
}
