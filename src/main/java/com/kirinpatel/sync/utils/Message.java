package com.kirinpatel.sync.utils;

import java.io.Serializable;

public abstract class Message implements Serializable {

    Object body;

    static abstract class Builder {

        Object body = null;

        public Builder() {

        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        abstract Message build();
    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        if (o instanceof Message) {
            Message message = (Message) o;
            return message.getType().equals(getType());
        } else {
            return false;
        }
    }

    public Object getBody() {
        return body;
    }

    public abstract String getType();
}
