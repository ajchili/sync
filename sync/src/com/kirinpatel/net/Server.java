/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.util.Message;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kirin Patel
 * @version 0.2
 */
public class Server {
    
    private boolean isRunning = true;
    private Thread serverThread;
    private ServerSocket service;
    private ArrayList<Socket> sockets = new ArrayList<>();
    
    public Server(int port) {
        try {
            service = new ServerSocket(port);
            
            serverThread = new Thread(new ServerThread());
            new Thread(new ServerSocketThread()).start();
            
            serverThread.start();
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error creating server. " + ex.getMessage());
            System.exit(1);
        }
    }
    
    public void stop() {
        isRunning = false;
        
        if (!service.isClosed()) {
            try {
                for (Socket socket : sockets) {
                    socket.close();
                }
                
                service.close();
                
                serverThread.join();
            } catch (IOException ex) {
                // Error handling
                System.out.println("Error closing server. " + ex.getMessage());
                System.exit(3);
            } catch (InterruptedException ex) {
                // Error handling
                System.out.println("Error closing server. " + ex.getMessage());
                System.exit(4);
            }
        }
    }
    
    private class ServerThread implements Runnable {

        @Override
        public void run() {
            ObjectInputStream input;
            ObjectOutputStream output;
            while(isRunning) {
                for (Socket socket : sockets) {
                    try {
                        input = new ObjectInputStream(socket.getInputStream());
                        output = new ObjectOutputStream(socket.getOutputStream());
                        
                        while(input.available() > -1) {
                            Message message = (Message) input.readObject();
                            switch(message.getType()) {
                                case 0:
                                    System.out.println("(" + sockets.indexOf(socket) + ") " + socket.getInetAddress() + ": Connected!");
                                    socket.close();
                                    sockets.remove(socket);
                                    break;
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                try {
                    wait(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private class ServerSocketThread implements Runnable {

        @Override
        public void run() {
            try {
                Socket socket = service.accept();
                socket.setKeepAlive(true);
                sockets.add(socket);
                
                new Thread(new Server.SocketThread(socket)).start();
            } catch (IOException ex) {
                // Error handling
                System.out.println(ex.hashCode() + " Error with server socket. " + ex.getMessage());
            }
        }
    }
    
    public class SocketThread implements Runnable {

        private Socket socket;
        
        public SocketThread(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            boolean isRunning = true;
            while(isRunning) {
                if (socket.isBound()) {
                    try {
                        System.out.println("(" + sockets.size() + ") " + socket.getInetAddress() + ": Connected!");
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject(new Message(0, 1));
                        new Thread(new ServerSocketThread()).start();
                        isRunning = false;
                    } catch (IOException ex) {
                        // Error handling
                        System.out.println("Error establishing connection with client. " + ex.getMessage());
                    }
                }
            }
        }
    }
}
