package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ClientInfoGUI extends JFrame {

    private User client;

    public ClientInfoGUI(int index) {
        super("Client info");

        client = Main.connectedUsers.get(index);

        setResizable(false);
        setLayout(new GridLayout(4, 1));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addComponentListener(new ClientInfoComponentListener());
        setLocationRelativeTo(null);

        add(new JLabel("Username: " + client.getUsername()));
        add(new JLabel("UserID: " + client.getUserID()));
        JButton disconnectUser = new JButton("Kick Client");
        disconnectUser.addActionListener(e -> {
            Server.kickUser(index);
            ControlPanel.isUserDisplayShown = false;
            dispose();
        });
        add(new JSlider());
        add(disconnectUser);
        pack();

        setVisible(true);
    }

    class ClientInfoComponentListener implements ComponentListener {

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
            ControlPanel.isUserDisplayShown = false;
            dispose();
        }
    }
}
