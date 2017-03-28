/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.*;
import java.awt.*;
import java.awt.event.*;
import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javax.swing.*;

/**
 *
 * @author Kirin Patel
 * @version 0.4
 * @see com.kirinpatel.Main
 * @see com.kirinpatel.net.Server
 * @see com.kirinpatel.net.Client
 * @see java.awt.GridLayout
 * @see java.awt.event.ComponentListener
 * @see javax.awt.JFrame
 * @see javax.awt.JButton
 * @see javax.awt.AbastractButton
 */
public class Window extends JFrame {
    
    private boolean isRunning = false;
    public static JTextArea textArea;
    public static JTextField textInput;
    
    /**
     * Main constructor that will create a Window with specified title and
     * type.
     * 
     * @param title Title of window
     * @param type Type of window
     */
    public Window(String title, int type) {
        super(title);
        
        switch(type) {
            case 0:
                createLauncher();
                break;
            case 1:
                createServer();
                break;
            case 2:
                createClient();
                break;
            default:
                System.exit(0);
        }
        
        setVisible(true);
    }
    
    /**
     * This method will create the launcher Window.
     */
    private void createLauncher() {
        setSize(400, 200);
        
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        JButton hostServer = new JButton("Host");
        hostServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.window.setVisible(false);
                new Window("sync - Server", 1);
            }
        });
        add(hostServer);
        
        JButton joinServer = new JButton("Join");
        joinServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.window.setVisible(false);
                new Window("sync - Client", 2);
            }
        });
        add(joinServer);
                
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    /**
     * This method will create the server Window.
     */
    private void createServer() {
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        
        createGUI(0);
        
        setResizable(true);
                
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                
            }

            @Override
            public void componentShown(ComponentEvent e) {
                
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                isRunning = false;
                dispose();
                Main.window.setVisible(true);
            }
        });
        
        isRunning = true;
        new Thread(new WindowThread(0)).start();
    }
    
    /**
     * This method will create the client Window.
     */
    private void createClient() {
        String ipAddress = JOptionPane.showInputDialog("Please enter the server IP address.");
        if (ipAddress == null) {
            dispose();
            Main.window.setVisible(true);
            return;
        }
        
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        
        setResizable(true);
                
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                
            }

            @Override
            public void componentShown(ComponentEvent e) {
                
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                isRunning = false;
                dispose();
                Main.window.setVisible(true);
            }
        });
        
        isRunning = true;
        new Thread(new WindowThread(1, ipAddress)).start();
    }
    
    private void createGUI(int type) {
        setLayout(new BorderLayout());
        
        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        
        JPanel interactionPanel = new JPanel(new GridLayout(3, 1));
        JPanel statusPanel = new JPanel();
        interactionPanel.add(statusPanel);
        JPanel controlPanel = new JPanel();
        interactionPanel.add(controlPanel);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setPreferredSize(new Dimension(256, 288));
        inputPanel.setMinimumSize(new Dimension(128, 192));
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(textArea);
        inputPanel.add(scroll, BorderLayout.CENTER);
        JPanel textPanel = new JPanel(new GridLayout(1, 2));
        textInput = new JTextField();
        textPanel.add(textInput);
        JButton sendMessage = new JButton("Send");
        textPanel.add(sendMessage);
        inputPanel.add(textPanel, BorderLayout.SOUTH);
        interactionPanel.add(inputPanel);
        
        add(interactionPanel, BorderLayout.EAST);
  
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });
    }

    private static void initFX(JFXPanel fxPanel) {
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }
    
    private static Scene createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);

        return (scene);
    }
    
    class WindowThread implements Runnable {

        private int type;
        private String ip;
        
        public WindowThread(int type) {
            this.type = type;
        }
        
        public WindowThread(int type, String ip) {
            this.type = type;
            this.ip = ip;
        }
        
        @Override
        public void run() {
            switch(type) {
                case 0:
                    Server server = null;
                    
                    new Thread(new ServerThread(server)).start();
                    
                    server = new Server();
                    break;
                case 1:
                    Client client = null;
                    
                    new Thread(new ClientThread(client)).start();
                    
                    client = new Client(ip);
                    break;
            }
        }
        
        public class ServerThread implements Runnable {

            private Server server;

            public ServerThread(Server server) {
                this.server = server;
            }

            @Override
            public void run() {
                while(isRunning) {

                }

                server.stop();
            }
        }
        
        public class ClientThread implements Runnable {
            
            private Client client;
            
            public ClientThread(Client client) {
                this.client = client;
            }
            
            @Override
            public void run() {
                while(isRunning) {
                    
                }
                
                client.stop();
            }
        }
    }
}
