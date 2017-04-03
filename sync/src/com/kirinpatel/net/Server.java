/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.util.Message;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kirin Patel
 * @version 0.5
 */
public class Server {
    
    public static int numberOfConnectedClients = 0;
    public static String mediaURL = "";
    
    private boolean isRunning = true;
    private ExecutorService connectionExecutor;
    private ServerSocket service;
    
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
    }
    
    public void stop() {
        isRunning = false;
    }
    
    class ServerSocketTask implements Runnable {
        
        private Socket socket;
        
        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            boolean hasConnected = false;
            
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            
           try {
                while(socket.getInputStream().available() < 0) {
                    
                }
                numberOfConnectedClients++;
                input = new ObjectInputStream(socket.getInputStream());
                
                Message connectionMessage = (Message) input.readObject();
                
                if (connectionMessage.getType() == 0 && (int) connectionMessage.getMessage() == 1) {
                    output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(new Message(0, 1));
                    output.flush();
                    hasConnected = true;
                    System.out.println("Client connected. (" + socket.getInetAddress() + ":" + socket.getPort() + ")");
                }
                    
                while(isRunning && hasConnected) {
                    Message message = (Message) input.readObject();
                    switch(message.getType()) {
                        case 0:
                            int messageBody = (int) message.getMessage();
                            switch(messageBody) {
                                case 0:
                                    hasConnected = false;
                                    break;
                                case 1:
                                    System.out.println("Client connected. (" + socket.getInetAddress() + ":" + socket.getPort() + ")");
                            }
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                    }
                }
            } catch (IOException ex) {
                // Error handling
                System.out.println("Error with socket. " + ex.getMessage());
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                // Error handling
                System.out.println("Error with message. " + ex.getMessage());
            } finally {
                try {
                    numberOfConnectedClients--;
                    if (input != null)
                        input.close();
                    if (output != null)
                        output.close();
                    socket.close();
                } catch (IOException ex) {
                    // Error handling
                    System.out.println("Error closing socket. " + ex.getMessage());
                }
            }
        }
    }
}
