package com.kirinpatel.sync.utils;

public class DisconnectingMessage extends Message {

    @Override
    public String toString() {
        return "Disconnecting.";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PingMessage;
    }
}
