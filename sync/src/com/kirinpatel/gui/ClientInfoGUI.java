package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ClientInfoGUI extends JFrame {

    private int index;
    private JLabel ping;
    private UpdateUIThread updateUIThread;

    public ClientInfoGUI(int index) {
        super("Client info");

        this.index = index;

        setResizable(false);
        setLayout(new GridLayout(4, 1));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addComponentListener(new ClientInfoComponentListener());
        setLocationRelativeTo(null);

        add(new JLabel("Username: " + Main.connectedUsers.get(index).getUsername()));
        add(new JLabel("UserID: " + Main.connectedUsers.get(index).getUserID()));
        ping = new JLabel("Ping: " + Main.connectedUsers.get(index).getPing() + " ms");
        add(ping);
        JButton disconnectUser = new JButton("Kick Client");
        disconnectUser.addActionListener(e -> {
            Server.kickUser(index);
            ControlPanel.isUserDisplayShown = false;
            dispose();
        });
        add(disconnectUser);
        pack();

        updateUIThread = new UpdateUIThread();
        new Thread(updateUIThread).start();

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
            updateUIThread.stop();
            dispose();
        }
    }

    class UpdateUIThread implements Runnable {

        private boolean isRunning = true;

        @Override
        public void run() {
            while(isRunning) {
                try {
                    Thread.sleep(500);
                    if (index <= Main.connectedUsers.size()) ping.setText("Ping: " + Main.connectedUsers.get(index).getPing() + " ms");
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            isRunning = false;
        }
    }
}
