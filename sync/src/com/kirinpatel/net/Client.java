/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.gui.Window;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;

/**
 *
 * @author Kirin Patel
 * @version 0.6
 */
public class Client {
    
    private String message;
    
    private Socket socket;
    private Window window;
    private boolean isConnected = false;
    private boolean sendMessage;
    
    public Client(String ip, Window window) {
        this.window = window;
        
        try {
            socket = new Socket(ip, 8000);
            socket.setKeepAlive(true);
            
            new Thread(new ClientThread()).start();
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error joining server. " + ex.getMessage());
            stop();
        }
    }
    
    public void sendMessage(String message) {
        this.message = message;
        sendMessage = true;
    }
    
    public synchronized void stop() {
        isConnected = false;
    }
    
    private class ClientThread implements Runnable {
        
        @Override
        public void run() {
            try {
                connectToServer();
                
                while (socket.getInputStream().available() < 0) {
                    
                }
                
                JsonObject object = Json.createReader(socket.getInputStream()).readObject();
                if (object.getInt("type") == 0)
                    isConnected = (object.getInt("message") == 2);
                
                sendUsername();
                
                while (isConnected) {
                    if (socket.getInputStream().available() > 0) {
                        object = Json.createReader(socket.getInputStream()).readObject();
                        switch(object.getInt("type")) {
                            case 10201:
                                displayMessage(object.getString("message"));
                                break;
                            case 10202:
                                setMediaURL(object.getString("message"));
                                break;
                            default:
                                System.out.println(object);
                                break;
                        }
                    }
                    
                    if (sendMessage)
                        sendMessage(message);
                }
                
                disconnectFromServer();
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
        
        private synchronized void flush() throws IOException {
            socket.getOutputStream().flush();
        }
        
        private synchronized void connectToServer() throws IOException {
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 0);
            messageBuilder.add("message", 1);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
        }
        
        private synchronized void disconnectFromServer() throws IOException {
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 0);
            messageBuilder.add("message", 0);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
        }
        
        private synchronized void setMediaURL(String mediaURL) {
            window.setMediaURL(mediaURL);
        }
        
        private synchronized void sendUsername() throws IOException {
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 10200);
            messageBuilder.add("message", System.getProperty("user.name"));
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
        }
        
        private synchronized void sendMessage(String message) throws IOException {
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 10201);
            messageBuilder.add("message", System.getProperty("user.name") + ": " + message);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
        }
        
        private synchronized void displayMessage(String message) {
            window.textArea.append(message);
        }
    }
}
