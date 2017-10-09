package com.kirinpatel.sync.utils;

public class MediaTimeMessage extends Message {

    @Override
    public String toString() {
        return "Media Time: " + body;
    }

    @Override
    public String getType() {
        return "mediaTimeMessage";
    }
}
