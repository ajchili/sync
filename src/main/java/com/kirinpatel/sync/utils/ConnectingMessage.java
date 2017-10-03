package com.kirinpatel.sync.utils;

public class ConnectingMessage  extends Message {

    @Override
    public String toString() {
        return "Connecting.";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PingMessage;
    }
}
