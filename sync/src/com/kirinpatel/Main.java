package com.kirinpatel;

import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.UIMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main class that will run the application. This class, as of version 2.0, will
 * also server as the launcher for the application.
 * 
 * @author Kirin Patel
 */
public class Main extends JFrame {

    public static boolean isDarkModeEnabled = false;

    /**
     * Creates launcher window.
     */
    public Main() {
        super("sync");

        Debug.Log("Starting sync launcher...", 3);
        setSize(400, 200);
        
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        JButton hostServer = new JButton("Host");
        hostServer.addActionListener(new LauncherButtonEvent(0));
        buttonPanel.add(hostServer);
        JButton joinServer = new JButton("Join");
        joinServer.addActionListener(new LauncherButtonEvent(1));
        buttonPanel.add(joinServer);
        add(buttonPanel, BorderLayout.CENTER);

        JCheckBox darkModeCheckBox = new JCheckBox("Dark mode");
        darkModeCheckBox.addActionListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            isDarkModeEnabled = box.isSelected();
        });
        add(darkModeCheckBox, BorderLayout.SOUTH);

        setVisible(true);
        Debug.Log("Sync launcher displayed.", 3);
    }
    
    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // new Main();
        new Testing();
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
                    if (ipAddress == null || ipAddress.isEmpty()) {
                        Debug.Log("No IP Address provided!", 2);
                        setVisible(true);
                    } else {
                        new Client(ipAddress);
                        dispose();
                    }
                    break;
            }
        }
    }
}
