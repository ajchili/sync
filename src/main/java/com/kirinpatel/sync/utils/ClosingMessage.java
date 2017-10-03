package com.kirinpatel.sync.utils;

public class ClosingMessage extends Message {

    @Override
    public String toString() {
        return "Closing Server.";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PingMessage;
    }
}
