/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.gui.MediaPanel;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author Kirin Patel
 * @version 0.1.0
 */
public class Client extends JFrame {
    
    private Socket socket;
    private MediaPanel mediaPanel;
    public static JTextArea textArea;
    public static JTextField textInput;
    
    private String username;
    private String message;
    private String ip;
    private boolean sendMessage;
    
    private boolean isConnected = false;
    
    public Client(String title, String ip) {
        super(title);
        this.ip = ip;
        
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        setMaximumSize(new Dimension(1280, 720));
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ClientComponentListener());
        
        mediaPanel = new MediaPanel();
        add(mediaPanel, BorderLayout.CENTER);
        
        JPanel interactionPanel = new JPanel(new GridLayout(3, 1));
        JPanel statusPanel = new JPanel();
        interactionPanel.add(statusPanel);
        JPanel controlPanel = new JPanel(new GridLayout(3, 1));
        JPanel inputPanel = new JPanel(new BorderLayout());
        
        interactionPanel.add(controlPanel);
        
        inputPanel.setPreferredSize(new Dimension(256, 288));
        inputPanel.setMinimumSize(new Dimension(128, 192));
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(textArea);
        inputPanel.add(textScroll, BorderLayout.CENTER);
        JPanel textPanel = new JPanel(new GridLayout(1, 2));
        textInput = new JTextField();
        textPanel.add(textInput);
        JButton send = new JButton("Send");
        send.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!textInput.getText().equals("")) {
                    message = textInput.getText();
                    sendMessage = true;
                    textInput.setText("");
                }
            }   
        });
        textPanel.add(send);
        inputPanel.add(textPanel, BorderLayout.SOUTH);
        interactionPanel.add(inputPanel);

        add(interactionPanel, BorderLayout.EAST);
        
        new Thread(new ClientTask()).start();
        
        setVisible(true);
    }
    
    public void stop() {
        isConnected = false;
    }
    
    public void sendMessage(String message) {
        this.message = message;
        sendMessage = true;
    }
    
    /**
     * Custom ComponentListener that will be used to handle client GUI events.
     */
    class ClientComponentListener implements ComponentListener {

        /**
         * Unsupported.
         * 
         * @param e ComponentEvent
         */
        @Override
        public void componentResized(ComponentEvent e) {
            // Not supported
        }

        /**
         * Unsupported.
         * 
         * @param e ComponentEvent
         */
        @Override
        public void componentMoved(ComponentEvent e) {
            // Not supported
        }

        /**
         * Unsupported.
         * 
         * @param e ComponentEvent
         */
        @Override
        public void componentShown(ComponentEvent e) {
            // Not supported
        }

        /**
         * Stop server on hide.
         * 
         * @param e ComponentEvent
         */
        @Override
        public void componentHidden(ComponentEvent e) {
            stop();
        }
    }
    
    class ClientTask implements Runnable {
        
        @Override
        public void run() {
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
                                textArea.append(object.getString("message"));
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
            mediaPanel.setMedia(mediaURL);
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
