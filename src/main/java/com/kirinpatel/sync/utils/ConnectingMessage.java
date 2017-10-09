package com.kirinpatel.sync.utils;

public class ConnectingMessage extends Message {

    @Override
    public String toString() {
        return "Connecting.";
    }

    @Override
    public String getType() {
        return "connecting";
    }
}
