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
    
    public synchronized void stop() {
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
                            case 10201:
                                // Recieved message
                                break;
                            default:
                                System.out.println(object);
                                break;
                        }
                    }
                    
                    if (sendURL)
                        sendURL();
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
