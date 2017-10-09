package com.kirinpatel.sync.utils;

public class MediaRateMessage extends Message {
    @Override
    public String toString() {
        return "Media Rate: " + body;
    }

    @Override
    public String getType() {
        return "mediaRateMessage";
    }
}
