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
 * @version 0.4
 */
public class Client {
    
    private Socket socket;
    boolean isConnected = false;
    
    public Client(String ip) {
        try {
            socket = new Socket(ip, 8000);
            socket.setKeepAlive(true);
            
            new Thread(new ClientThread()).start();
        } catch (IOException ex) {
            // Error handling
            System.out.println("Error joining server. " + ex.getMessage());
            System.exit(0);
        }
    }
    
    public void stop() {
        isConnected = false;
    }
    
    private class ClientThread implements Runnable {
        
        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message(0, 1));
                output.flush();
                
                while(socket.getInputStream().available() < 0) {
                    
                }
                input = new ObjectInputStream(socket.getInputStream());
                
                Message status = (Message) input.readObject();
                isConnected = (int) status.getMessage() == 1;
                System.out.println(isConnected);
                
                while(isConnected) {
                    
                }
                
                output.writeObject(new Message(0, 0));
            } catch (IOException ex) {
                // Error handling
                System.out.println("Error joining server. " + ex.getMessage());
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    input.close();
                    output.close();
                    socket.close();
                } catch (IOException ex) {
                    // Error handling
                    System.out.println("Error stopping client. " + ex.getMessage());
                    System.exit(1);
                }
            }
        }
    }
}
