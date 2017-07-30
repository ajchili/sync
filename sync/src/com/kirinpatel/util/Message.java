package com.kirinpatel.util;

import java.io.Serializable;

/**
 * Message object used to send/receive data between clients and a server.
 */
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
        MEDIA(20),
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

    /**
     * Provides printable version of message.
     *
     * @return Returns message
     */
    @Override
    public String toString() {
        return "[" + type + "] " + message.toString();
    }

    /**
     * Determines if provided message is equal to this one.
     *
     * @param o Message object
     * @return Returns if equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message))
            return false;
        else {
            Message objM = (Message) o;
            return (type == objM.type) && (message.equals(objM.message));
        }
    }

    /**
     * Provides type of message.
     *
     * @return Returns message type
     */
    public MESSAGE_TYPE getType() {
        return type;
    }

    /**
     * Provides message.
     *
     * @return Returns message
     */
    public Object getMessage() {
        return message;
    }
}
