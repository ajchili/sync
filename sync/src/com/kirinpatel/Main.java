package com.kirinpatel;

import com.kirinpatel.net.*;
import com.kirinpatel.util.UIMessage;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Main class that will run the application. This class, as of version 2.0, will
 * also server as the launcher for the application.
 * 
 * @author Kirin Patel
 * @version 2.0.2
 */
public class Main extends JFrame {
    
    public static Main main;
    
    /**
     * Creates launcher window.
     * 
     * @param title Title
     */
    public Main(String title) {
        super(title);
        
        setSize(400, 200);
        
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        JButton hostServer = new JButton("Host");
        hostServer.addActionListener(new LauncherButtonEvent(0));
        add(hostServer);
        
        JButton joinServer = new JButton("Join");
        joinServer.addActionListener(new LauncherButtonEvent(1));
        add(joinServer);
                
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setVisible(true);
    }
    
    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        main = new Main("sync");
    }
    
    /**
     * Custom ActionListener that will serve to enable usability of Launcher
     * JButtons.
     */
    class LauncherButtonEvent implements ActionListener {

        private int type;
        
        /**
         * Main constructor that will establish the ActionListener with the
         * given type.
         * 
         * @param type Type
         */
        public LauncherButtonEvent(int type) {
            this.type = type;
        }
        
        /**
         * Code that will be executed on ActionEvent.
         * 
         * @param e ActionEvent
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            
            switch(type) {
                case 0:
                    new Server();
                    dispose();
                    break;
                case 1:
                    String ipAddress = UIMessage.getInput("sync", "Please enter the ip address of the server.");
                    if (ipAddress == null) {
                        setVisible(true);
                        break;
                    }

                    dispose();
                    break;
            }
        }
    }
}
