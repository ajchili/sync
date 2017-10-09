package com.kirinpatel.sync.utils;

public class ClientNameMessage extends Message {

    static class Builder extends Message.Builder {

        @Override
        Message build() {
            return new ClientNameMessage(this);
        }
    }

    public ClientNameMessage(Builder builder) {
        body = builder.body;
    }

    @Override
    public String toString() {
        return "Client Name: " + body;
    }

    @Override
    public String getType() {
        return "clientNameMessage";
    }
}
