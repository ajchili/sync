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
import javax.swing.*;

/**
 *
 * @author Kirin Patel
 * @version 0.3
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
        Server server = new Server(12345);
        
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        
        setResizable(true);
                
        setDefaultCloseOperation(HIDE_ON_CLOSE);
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
                server.stop();
                dispose();
                Main.window.setVisible(true);
            }
        });
        setLocationRelativeTo(null);
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
        
        Client client = new Client(ipAddress, 12345);
        
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        
        setResizable(true);
                
        setDefaultCloseOperation(HIDE_ON_CLOSE);
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
                client.stop();
                dispose();
                Main.window.setVisible(true);
            }
        });
        setLocationRelativeTo(null);
    }
}
