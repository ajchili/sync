/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.json.*;

/**
 *
 * @author Kirin Patel
 * @version 0.7
 */
public class Server {
    
    public static int numberOfConnectedClients = 0;
    private static String mediaURL = "";
    private String message;
    
    private boolean isRunning = true;
    private ExecutorService connectionExecutor;
    private ServerSocket service;
    private static boolean sendURL = false;
    private static boolean sendMessage = false;
    private static boolean sendUserMessage = false;
    private static ArrayList<String> connectedUsers = new ArrayList<>();
    
    public Server() {
        connectionExecutor = Executors.newFixedThreadPool(10);
        try {
            service = new ServerSocket(8000);
            
            Socket socket;
            
            while(isRunning) {
                socket = service.accept();
                
                connectionExecutor.execute(new ServerSocketTask(socket));
            }
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error creating server. " + ex.getMessage());
            isRunning = false;
        } finally {
            isRunning = false;
            
            if (!service.isClosed()) {
                try {
                    service.close();

                    connectionExecutor.shutdown();

                    while (!connectionExecutor.isTerminated()) {

                    }
                } catch (IOException ex) {
                    // Error handling
                    System.out.println("Error closing server. " + ex.getMessage());
                    System.exit(1);
                }
            }
        }
    }
    
    public static void setMediaURL(String mediaURL) {
        Server.mediaURL = mediaURL;
        sendURL = true;
    }
    
    public void sendMessage(String message) {
        if (this.message.equals(""))
            this.message = message;
        else 
            this.message += "\n" + message;
        
        sendMessage = true;
    }
    
    public synchronized void stop() {
        isRunning = false;
    }
    
    class ServerSocketTask implements Runnable {
        
        private String username;
        private final Socket socket;
        
        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            boolean hasConnected = false;
            
            try {
                waitForMessage();
                
                JsonObject object = Json.createReader(socket.getInputStream()).readObject();
                
                if (object.getInt("type") ==  0) {
                    hasConnected = object.getInt("message") == 1;
                    numberOfConnectedClients++;
                    
                    // Send connection message
                    
                    JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
                    messageBuilder.add("type", 0);
                    messageBuilder.add("message", 2);
                    Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
                    flush();
                    
                    // Send current media
                    sendURL();
                }
                
                while (isRunning && hasConnected) {
                    if (socket.getInputStream().available() > 0) {
                        object = Json.createReader(socket.getInputStream()).readObject();
                        switch(object.getInt("type")) {
                            case 0:
                                switch(object.getInt("message")) {
                                    case 0:
                                        numberOfConnectedClients--;
                                        break;
                                }
                                break;
                            case 10200:
                                username = object.getString("message");
                                connectedUsers.add(username);
                                break;
                            case 10201:
                                sendMessage(object.getString("message"));
                                break;
                            default:
                                System.out.println(object);
                                break;
                        }
                    }
                    
                    if (sendURL)
                        sendURL();
                    
                    if (sendMessage) {
                        
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    numberOfConnectedClients--;
                    connectedUsers.remove(username);
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        private void waitForMessage() throws IOException {
            while (socket.getInputStream().available() < 0) {
                    
            }
        }
        
        private synchronized void flush() throws IOException {
            socket.getOutputStream().flush();
        }
        
        private synchronized void sendURL() throws IOException {
            if (!mediaURL.equals("")) {
                JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
                messageBuilder.add("type", 10202);
                messageBuilder.add("message", mediaURL);
                Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
                flush();
            }
            
            sendURL = false;
        }
    }
}
