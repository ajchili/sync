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

/**
 *
 * @author Kirin Patel
 * @version 0.3
 */
public class Client {
    
    private Socket socket;
    private Thread clientThread;
    
    public Client(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            
            clientThread = new Thread(new ClientThread());
            clientThread.start();
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error joining server. " + ex.getMessage());
            System.exit(2);
        }
    }
    
    public void stop() {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(new Message(0, 0));
            
            socket.close();
            clientThread.join();
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error stopping client. " + ex.getMessage());
            System.exit(4);
        } catch (InterruptedException ex) {
            // Error handling
            System.out.println("Error stopping client. " + ex.getMessage());
            System.exit(5);
        }
    }
    
    private class ClientThread implements Runnable {

        @Override
        public void run() {
            boolean hasConnected = false;
            
            try {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                
                Message status = (Message) input.readObject();
                System.out.println((int) status.getMessage() == 1);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
