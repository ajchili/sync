package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.net.Server;
import com.kirinpatel.sync.net.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

class ClientInfoGUI extends JFrame {

    private final User user;
    private final JLabel ping;
    private final JLabel mediaTime;
    private final JLabel mediaState;
    private final UpdateUIThread updateUIThread;

    ClientInfoGUI(User user) {
        super("Client info");

        this.user = user;
        setResizable(false);
        setLayout(new GridLayout(6, 1));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addComponentListener(new ClientInfoComponentListener());
        setLocationRelativeTo(null);

        add(new JLabel("Username: " + user.getUsername()));
        add(new JLabel("UserID: " + user.getUserID()));
        ping = new JLabel("Ping: " + user.getPing() + " ms");
        add(ping);
        mediaTime = new JLabel("Playback Time: "
                + user.getMedia().getFormattedTime()
                + ":" + user.getMedia().getCurrentTime() % 1000);
        add(mediaTime);
        mediaState = new JLabel("Playback State: "
                + (user.getMedia().isPaused() ? "Paused" : "Playing"));
        add(mediaState);
        JButton disconnectUser = new JButton("Kick Client");
        updateUIThread = new UpdateUIThread();
        disconnectUser.addActionListener(e -> {
            Server.kickUser(user);
            ControlPanel.isUserDisplayShown = false;
            updateUIThread.stop();
            dispose();
        });
        add(disconnectUser);
        pack();

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
                    Thread.sleep(250);
                    ping.setText("Ping: " + user.getPing() + " ms");
                    mediaTime.setText("Playback Time: "
                            + user.getMedia().getFormattedTime()
                            + ":" + user.getMedia().getCurrentTime() % 1000);
                    mediaState.setText("Playback State: "
                            + (user.getMedia().isPaused() ? "Paused" : "Playing"));
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        void stop() {
            isRunning = false;
        }
    }
}
