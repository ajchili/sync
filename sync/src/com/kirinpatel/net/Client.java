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
 * @version 0.7
 */
public class Client {
    
    private String username;
    private String message;
    private boolean sendMessage;
    
    private Socket socket;
    private Window window;
    private boolean isConnected = false;
    
    public Client(String ip, Window window) {
        this.window = window;
        username = System.getProperty("user.name");
        
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
    
    public void stop() {
        isConnected = false;
    }
    
    public void sendMessage(String message) {
        this.message = message;
        sendMessage = true;
    }
    
    class ClientThread implements Runnable {
       
        @Override
        public void run() {
            try {
                connectToServer();
                waitForMessage();
                
                JsonObject object = Json.createReader(socket.getInputStream()).readObject();
                if (object.getInt("type") == 0)
                    isConnected = (object.getInt("message") == 2);
                
                sendUsername();
                
                while (isConnected) {
                    if (socket.getInputStream().available() > 0) {
                        object = Json.createReader(socket.getInputStream()).readObject();
                        switch(object.getInt("type")) {
                            case 10201:
                                // Recieved message
                                window.addMessage(object.getString("message"));
                                break;
                            case 10202:
                                // Recieve url
                                setMediaURL(object.getString("message"));
                                break;
                            default:
                                System.out.println(object);
                                break;
                        }
                    }
                    
                    if (sendMessage)
                        sendMessage();
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
        
        private synchronized void waitForMessage() throws IOException {
            while (socket.getInputStream().available() < 0 && isConnected) {
            
            }
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
            messageBuilder.add("message", username);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
        }
        
        private synchronized void sendMessage() throws IOException {
            sendMessage = false;
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 10201);
            messageBuilder.add("message", message);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
        }
    }
}
