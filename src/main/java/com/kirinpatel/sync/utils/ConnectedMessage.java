package com.kirinpatel.sync.utils;

public class ConnectedMessage extends Message {

    @Override
    public String toString() {
        return "Connected.";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PingMessage;
    }
}
