/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.gui.Window;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.json.*;

/**
 *
 * @author Kirin Patel
 * @version 0.8
 */
public class Server {
    
    private static String mediaURL = "";
    private String message;
    private boolean sendMessage;
    
    private boolean isRunning = true;
    private Window window;
    private ExecutorService connectionExecutor;
    private ServerSocket service;
    private static boolean sendURL = false;
    
    public Server(Window window) {
        this.window = window;
        connectionExecutor = Executors.newFixedThreadPool(10);
        
        try {
            service = new ServerSocket(8000);
            service.setReuseAddress(true);
            
            Socket socket;
            
            while(isRunning) {
                socket = service.accept();
                
                connectionExecutor.execute(new ServerSocketTask(socket));
            }
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error creating server. " + ex.getMessage());
        } finally {
            isRunning = false;
            
            if (service != null)
                if (!service.isClosed())
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
    
    /**
     * Stops server.
     */
    public void stop() {
        isRunning = false;
    }
    
    public static void setMediaURL(String mediaURL) {
        Server.mediaURL = mediaURL;
        sendURL = true;
    }
    
    public void sendMessage(String message) {
        this.message = message;
        sendMessage = true;
    }
    
    class ServerTask implements Runnable {
        
        @Override
        public void run() {
            Socket socket;
            
            try {
                while(isRunning) {
                    socket = service.accept();

                    connectionExecutor.execute(new ServerSocketTask(socket));
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
                                        // User disconnected
                                        break;
                                }
                                break;
                            case 10200:
                                username = object.getString("message");
                            case 10201:
                                // Recieved message
                                sendMessage(username + ": " + object.getString("message") + "\n");
                                break;
                            default:
                                System.out.println(object);
                                break;
                        }
                    }
                    
                    if (sendURL)
                        sendURL();
                    
                    if (sendMessage)
                        sendMessage(System.getProperty("user.name") + ": " + message + "\n");
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        private synchronized void waitForMessage() throws IOException {
            while (socket.getInputStream().available() < 0 && isRunning) {
            
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
        
        private synchronized void sendMessage(String message) throws IOException {
            if (sendMessage)
                sendMessage = false;
            
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 10201);
            messageBuilder.add("message", message);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
            
            window.addMessage(message);
        }
    }
}
