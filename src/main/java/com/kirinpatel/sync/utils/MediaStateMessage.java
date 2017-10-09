package com.kirinpatel.sync.utils;

public class MediaStateMessage extends Message {

    @Override
    public String toString() {
        return "Media State: " + body;
    }

    @Override
    public String getType() {
        return "mediaStateMessage";
    }
}
