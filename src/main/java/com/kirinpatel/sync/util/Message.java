package com.kirinpatel.sync.util;

import java.io.Serializable;

public class Message implements Serializable {

    private MESSAGE_TYPE type;
    private Object message;

    public enum MESSAGE_TYPE {
        DISCONNECTING(0),
        CONNECTING(1),
        CONNECTED(2),
        CLOSING(3),
        PING(4),
        CLIENT_NAME(10),
        CONNECTED_CLIENTS(11),
        MEDIA_URL(20),
        MEDIA_TIME(21),
        MEDIA_RATE(22),
        MEDIA_STATE(23),
        MESSAGES(30),
        CLIENT_MESSAGES(31);

        private int messageId;

        MESSAGE_TYPE(int messageId) {
            this.messageId = messageId;
        }

        public int getMessageId() {
            return messageId;
        }
    }

    public Message(MESSAGE_TYPE type, Object message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + message.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message))
            return false;
        else {
            Message objM = (Message) o;
            return (type == objM.type) && (message.equals(objM.message));
        }
    }

    public MESSAGE_TYPE getType() {
        return type;
    }

    public Object getMessage() {
        return message;
    }
}
