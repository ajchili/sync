/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.util;

import java.io.Serializable;

/**
 * Message object used to send data between clients and a server.
 *
 * @author Kirin Patel
 * @version 1.1
 */
public class Message implements Serializable {
    
    private int type;
    private Object message;
    
    /**
     * Main constructor that will create the message with a given type and the
     * provided message.
     * 
     * Type:
     *   0 - Connection message
     * 100 - Object is double
     * 101 - Object is boolean
     * 102 - Object is String
     *       10200 - String is username
     *       10201 - String is message
     *       10202 - String is URL
     * 103 - Next object is object
     *       10300 - Object is message
     * 
     * Message:
     *   TYPE 0:
     *       0 - Connection ending/failure
     *       1 - Attempting connection
     *       2 - Connection established
     * 
     * @param type Type of message being sent
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
