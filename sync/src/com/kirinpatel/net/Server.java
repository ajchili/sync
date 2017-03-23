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
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kirin Patel
 * @version 0.3
 */
public class Server {
    
    private boolean isRunning = true;
    private Thread serverThread;
    private ExecutorService executor;
    private ServerSocket service;
    private ArrayList<Socket> sockets = new ArrayList<>();
    
    public Server(int port) {
        executor = Executors.newCachedThreadPool();
        
        try {
            service = new ServerSocket(port);
            
            serverThread = new Thread(new ServerThread());
            
            executor.execute(serverThread);
            executor.execute(new ServerSocketThread());
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
                
                executor.shutdown();
                
                while (!executor.isTerminated()) {
                    
                }
            } catch (IOException ex) {
                // Error handling
                System.out.println("Error closing server. " + ex.getMessage());
                System.exit(3);
            }
        }
    }
    
    private class ServerThread implements Runnable {

        @Override
        public void run() {
            Socket socket;
            ObjectInputStream input;
            ObjectOutputStream output;
            boolean isConnected;
            Iterator<Socket> iterator;
            
            while(isRunning) {
                iterator = sockets.iterator();
                
                while(iterator.hasNext()) {
                    socket = iterator.next();
                    isConnected = true;
                    
                    try {
                        input = new ObjectInputStream(socket.getInputStream());
                        output = new ObjectOutputStream(socket.getOutputStream());
                        
                        while(input.available() > -1 && isConnected) {
                            Message message = (Message) input.readObject();
                            switch(message.getType()) {
                                case 0:
                                    switch((int) message.getMessage()) {
                                        case 0:
                                            System.out.println("(" + sockets.indexOf(socket) + ") " + socket.getInetAddress() + ": Disconnected!");
                                            input.close();
                                            output.close();
                                            socket.close();
                                            iterator.remove();
                                            isConnected = false;
                                            break;
                                    }
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
                    Thread.sleep(1000/30, 0);
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
                
                executor.execute(new Server.SocketThread(socket));
            } catch (IOException ex) {
                // Error handling
                System.out.println("Error with server socket. " + ex.getMessage());
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
                        executor.execute(new ServerSocketThread());
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
