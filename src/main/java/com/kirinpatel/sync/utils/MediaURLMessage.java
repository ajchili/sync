package com.kirinpatel.sync.utils;

public class MediaURLMessage extends Message {

    public class Builder extends Message.Builder {

        @Override
        Message build() {
            return new MediaURLMessage(this);
        }
    }

    public MediaURLMessage(Builder builder) {
        body = builder.body;
    }

    @Override
    public String toString() {
        return "Media URL: " + body;
    }

    @Override
    public String getType() {
        return "mediaURLMessage";
    }
}
