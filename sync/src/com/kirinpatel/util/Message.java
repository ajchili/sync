/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.util;

import java.io.Serializable;

/**
 * Message object used to send/comprehend data between clients and a server.
 */
public class Message implements Serializable {

    private int type;
    private Object message;

    /**
     * Main constructor that will create the message with a given type and the
     * provided message.
     *
     * Types:
     *  0: Connection Messages
     *      0: Disconnecting
     *      1: Connecting
     *      2: Connected
     *      3: Closing
     *  1#: Username Messages
     *      0: Sending client name
     *          User
     *      1: Sending connected clients
     *          ArrayList<User>
     *  2#: Media Messages
     *      0: MediaURL
     *          String
     *      1: Media state
     *          boolean
     *      2: Seek to point
     *          long
     *  3#: Messages
     *      0: Messages
     *          ArrayList<String>
     *      1: Client messages
     *          ArrayList<String>
     *
     * @param type    Type of message being sent
     * @param message Message to be sent
     */
    public Message(int type, Object message) {
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
    public int getType() {
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
