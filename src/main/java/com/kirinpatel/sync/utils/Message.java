package com.kirinpatel.sync.utils;

import java.io.Serializable;

public abstract class Message implements Serializable {

    Object body;

    abstract class Builder {

        private Object body = null;

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
    public abstract boolean equals(Object o);

    public Object getBody() {
        return body;
    }
}
