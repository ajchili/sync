/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.net;

import com.kirinpatel.gui.MediaPanel;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.json.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class will run the server. All communications, UI elements, and server
 * actions are carried out through this class.
 *
 * @author Kirin Patel
 * @version 0.1.1
 */
public class Server extends JFrame {
    
    private ExecutorService connectionExecutor;
    private ServerSocket service;
    private MediaPanel mediaPanel;
    public static JTextArea textArea;
    public static JTextField textInput;
    
    private static String mediaURL = "";
    private String message;
    
    private boolean isRunning = true;
    private static boolean sendURL = false;
    private boolean sendMessage = false;
    
    /**
     * Main constructor that will setup the server.
     * 
     * @param title Title
     */
    public Server(String title) {
        super(title);
        
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        setMaximumSize(new Dimension(1280, 720));
        setResizable(true);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ServerComponentListener());
        
        mediaPanel = new MediaPanel();
        add(mediaPanel, BorderLayout.CENTER);
        
        JPanel interactionPanel = new JPanel(new GridLayout(3, 1));
        JPanel statusPanel = new JPanel();
        interactionPanel.add(statusPanel);
        JPanel controlPanel = new JPanel(new GridLayout(3, 1));
        JPanel inputPanel = new JPanel(new BorderLayout());
        
        JTextField url = new JTextField();
        url.setColumns(25);
        JScrollPane urlScroll = new JScrollPane(url);
        controlPanel.add(urlScroll);
        JButton setVideoURL = new JButton("Set URL");
        setVideoURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaURL = url.getText();
                mediaPanel.setMedia(mediaURL);
                setMediaURL();
            }
        });
        controlPanel.add(setVideoURL);
        JPanel videoControls = new JPanel(new GridLayout(1, 3));
        controlPanel.add(videoControls);
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
        
        new Thread(new ServerTask()).start();
        
        setVisible(true);
    }
    
    /**
     * Stops server.
     */
    private void stop() {
        dispose();
        isRunning = false;
    }
    
    /**
     * Sets mediaURL for server and client.
     */
    private static void setMediaURL() {
        sendURL = true;
    }
    
    /**
     * Send message to clients.
     * 
     * @param message 
     */
    public void sendMessage(String message) {
        this.message = message;
        sendMessage = true;
    }
    
    /**
     * Custom ComponentListener that will be used to handle server GUI events.
     */
    class ServerComponentListener implements ComponentListener {

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
    
    /**
     * This class handles the establishment of connections between a client and
     * a server.
     */
    class ServerTask implements Runnable {
        
        /**
         * This method will run the startup and maintenance code of the server on
         * a separate thread.
         */
        @Override
        public void run() {
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
    }
    
    /**
     * This class will handle all communication between clients and server.
     */
    class ServerSocketTask implements Runnable {
        
        private String username;
        private final Socket socket;
        
        /**
         * Main constructor that will establish a connection between a client
         * and the server for communication.
         * 
         * @param socket Socket that a client has connected with
         */
        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }
        
        /**
         * This method will run the client and server communication code.
         */
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
        
        /**
         * Waits for message from client.
         * 
         * @throws IOException
         */
        private synchronized void waitForMessage() throws IOException {
            while (socket.getInputStream().available() < 0 && isRunning) {
            
            }
        }
        
        /**
         * Flushes output stream to client.
         * 
         * @throws IOException 
         */
        private synchronized void flush() throws IOException {
            socket.getOutputStream().flush();
        }
        
        /**
         * Sends mediaURL to client.
         * 
         * @throws IOException 
         */
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
        
        /**
         * Sends message to client.
         * 
         * @param message Message
         * @throws IOException 
         */
        private synchronized void sendMessage(String message) throws IOException {
            if (sendMessage)
                sendMessage = false;
            
            JsonObjectBuilder messageBuilder = Json.createObjectBuilder();
            messageBuilder.add("type", 10201);
            messageBuilder.add("message", message);
            Json.createWriter(socket.getOutputStream()).writeObject(messageBuilder.build());
            flush();
            
            textArea.append(message);
        }
    }
}
