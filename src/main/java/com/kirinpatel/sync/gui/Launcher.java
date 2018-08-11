package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.net.NetworkUser;
import com.kirinpatel.sync.net.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

public final class Launcher extends JFrame  {

    public static final Launcher INSTANCE = new Launcher();
    public NetworkUser connectedUser;

    private Launcher() {}

    public void open() {
        setTitle("sync");
        setSize(new Dimension(225, 115));
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton hostServer = new JButton("Host");
        hostServer.addActionListener(new LauncherButtonEvent(SERVER));
        buttonPanel.add(hostServer);
        JButton joinServer = new JButton("Join");
        joinServer.addActionListener(new LauncherButtonEvent(CLIENT));
        buttonPanel.add(joinServer);
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
        Sync.connectedUsers.clear();
        ControlPanel.showUserTimes = false;
    }

    class LauncherButtonEvent implements ActionListener, ServerIPAddressInputListener {

        private PlaybackPanel.PANEL_TYPE type;

        LauncherButtonEvent(PlaybackPanel.PANEL_TYPE type) {
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);

            switch(type) {
                case SERVER:
                    new Server();
                    setVisible(false);
                    break;
                case CLIENT:
                    new ServerIPAddressInput().setListener(this);
                    break;
            }
        }

        @Override
        public void closed() {
            setVisible(true);
        }
    }
}