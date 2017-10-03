package com.kirinpatel.sync.utils;

public class ClientNameMessage extends Message {

    @Override
    public String toString() {
        return "Client Name: " + body;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ClientNameMessage) {
            ClientNameMessage message = (ClientNameMessage) o;
            return body.equals(message.body);
        }
        return false;
    }
}
