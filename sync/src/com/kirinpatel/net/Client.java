/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.util.Message;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;

/**
 *
 * @author Kirin Patel
 * @version 0.5
 */
public class Client {
    
    public static String mediaURL = "";
    
    private Socket socket;
    boolean isConnected = false;
    
    public Client(String ip) {
        try {
            socket = new Socket(ip, 8000);
            socket.setKeepAlive(true);
            
            new Thread(new ClientThread()).start();
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error joining server. " + ex.getMessage());
            System.exit(0);
        }
    }
    
    public void stop() {
        isConnected = false;
    }
    
    private class ClientThread implements Runnable {
        
        @Override
        public void run() {
            try {
                JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
                messageBuilder.add("type", 0);
                messageBuilder.add("message", 1);
                Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
                flush();
                
                while (socket.getInputStream().available() < 0) {
                    
                }
                
                JsonObject object = Json.createReader(socket.getInputStream()).readObject();
                if (object.getInt("type") == 0)
                    isConnected = (object.getInt("message") == 2);
                
                while (isConnected) {
                    if (socket.getInputStream().available() > 0) {
                        object = Json.createReader(socket.getInputStream()).readObject();
                        switch(object.getInt("type")) {
                            case 10202:
                                System.out.println(object);
                                break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        private void flush() throws IOException {
            socket.getOutputStream().flush();
        }
    }
}
