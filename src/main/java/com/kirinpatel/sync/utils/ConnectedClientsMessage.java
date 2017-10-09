package com.kirinpatel.sync.utils;

import java.util.ArrayList;

public class ConnectedClientsMessage extends Message {

    @Override
    public String toString() {
        String clients = "[ ";
        for (User client : (ArrayList<User>) body) {
            clients += client.getUsername() + ", ";
        }
        clients += " ]";
        return "Connected Clients: " + clients;
    }

    @Override
    public String getType() {
        return "connectedClientsMessage";
    }
}
