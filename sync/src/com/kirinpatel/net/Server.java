/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;

/**
 *
 * @author Kirin Patel
 * @version 0.6
 */
public class Server {
    
    public static int numberOfConnectedClients = 0;
    public static String mediaURL = "";
    
    private boolean isRunning = true;
    private ExecutorService connectionExecutor;
    private ServerSocket service;
    private static boolean sendURL = false;
    
    public Server() {
        connectionExecutor = Executors.newFixedThreadPool(10);
        try {
            service = new ServerSocket(8000);
            
            Socket socket;
            
            while(true) {
                socket = service.accept();
                
                connectionExecutor.execute(new ServerSocketTask(socket));
            }
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error creating server. " + ex.getMessage());
            System.exit(0);
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
    
    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
        sendURL = true;
    }
    
    public static void sendURL(boolean b) {
        sendURL = b;
    }
    
    public void stop() {
        isRunning = false;
    }
    
    class ServerSocketTask implements Runnable {
        
        private final Socket socket;
        
        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            boolean hasConnected = false;
            
            try {
                while (socket.getInputStream().available() < 0) {
                    
                }
                
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
                        
                    }
                    
                    if (sendURL) {
                        sendURL();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        private void flush() throws IOException {
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
