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
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javax.swing.*;

/**
 *
 * @author Kirin Patel
 * @version 0.7
 * @see com.kirinpatel.Main
 * @see com.kirinpatel.net.Server
 * @see com.kirinpatel.net.Client
 */
public class Window extends JFrame {
    
    private boolean isRunning = false;
    private static String mediaURL = "";
    private static String message = "";
    
    public static JTextArea textArea;
    public static JTextField textInput;
    private JFXPanel fxPanel;
    
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
        setMaximumSize(new Dimension(1280, 720));
        
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
        setMaximumSize(new Dimension(1280, 720));
        
        createGUI(1);
        
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
        new Thread(new WindowThread(1, ipAddress, this)).start();
    }
    
    private void createGUI(int type) {
        setLayout(new BorderLayout());
        
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        
        JPanel interactionPanel = new JPanel(new GridLayout(3, 1));
        JPanel statusPanel = new JPanel();
        interactionPanel.add(statusPanel);
        JPanel controlPanel = new JPanel(new GridLayout(3, 1));
        JPanel inputPanel = new JPanel(new BorderLayout());
        
        switch(type) {
            case 0:
                JTextField url = new JTextField();
                url.setColumns(25);
                JScrollPane urlScroll = new JScrollPane(url);
                controlPanel.add(urlScroll);
                JButton setVideoURL = new JButton("Set URL");
                setVideoURL.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mediaURL = url.getText();
                        Server.setMediaURL(mediaURL);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                initFX(fxPanel);
                            }
                        });
                    }
                });
                controlPanel.add(setVideoURL);
                JPanel videoControls = new JPanel(new GridLayout(1, 3));
                controlPanel.add(videoControls);
                interactionPanel.add(controlPanel);
                break;
            case 1:
                break;
        }
        
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
        JButton sendMessage = new JButton("Send");
        sendMessage.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switch(type) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }   
        });
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
    
    // TODO: Add media controls for server side
    private static Scene createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);
        
        if (!mediaURL.equals("")) {
            Media media = new Media(mediaURL);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            MediaView mediaView = new MediaView(mediaPlayer);

            StackPane p = new StackPane();
            p.getChildren().add(mediaView);
            p.setStyle("-fx-background-color: #000000;");

            StackPane.setAlignment(mediaView, Pos.CENTER);
            scene = new Scene(p);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());
        }
        
        return (scene);
    }
    
    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
        });
    }
    
    class WindowThread implements Runnable {

        private int type;
        private Window window;
        private String ip;
        
        public WindowThread(int type) {
            this.type = type;
        }
        
        public WindowThread(int type, String ip, Window window) {
            this.type = type;
            this.ip = ip;
            this.window = window;
        }
        
        @Override
        public void run() {
            switch(type) {
                case 0:
                    new Thread(new ServerThread()).start();
                    
                    break;
                case 1:
                    new Thread(new ClientThread(ip, window)).start();
                    break;
            }
        }
        
        public class ServerThread implements Runnable {

            private Server server;

            @Override
            public void run() {
                server = new Server();
            
                server.stop();
            }
            
            public void setVideoUrl(String mediaURL) {
                server.setMediaURL(mediaURL);
            }
            
            public void sendMessage(String message) {
                server.sendMessage(message);
            }
        }
        
        public class ClientThread implements Runnable {
            
            private Client client;
            private String ip;
            private Window window;
            
            public ClientThread(String ip, Window window) {
                this.ip = ip;
                this.window = window;
            }
            
            @Override
            public void run() {
                client = new Client(ip, window);
                
                client.stop();
            }
            
            public void sendMessage(String message) {
                client.sendMessage(message);
            }
        }
    }
}
