package com.kirinpatel.sync;

import com.google.common.collect.ImmutableList;
import com.kirinpatel.sync.gui.ControlPanel;
import com.kirinpatel.sync.gui.PlaybackPanel;
import com.kirinpatel.sync.net.Client;
import com.kirinpatel.sync.net.NetworkUser;
import com.kirinpatel.sync.net.Server;
import com.kirinpatel.sync.util.UIMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public final class Launcher extends JFrame {

    public static final Launcher INSTANCE = new Launcher();
    public NetworkUser connectedUser;

    private Launcher() {}

    public void open() {
        setTitle("sync");
        setSize(new Dimension(200, 100));
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

    public static void saveIPAddress(String ipAddress) {
        if (getPreviousAddresses().contains(ipAddress)) {
            return;
        }
        Path dataPath = Paths.get("launcherData.dat");
        try {
            Files.write(
                    dataPath,
                    Collections.singletonList(ipAddress),
                    UTF_8,
                    Files.exists(dataPath) ? APPEND : CREATE);
        } catch (IOException e) {
            UIMessage.showErrorDialog(e, "Couldn't save IP address!");
        }
    }

    private static ImmutableList<String> getPreviousAddresses() {
        Path dataPath = Paths.get("launcherData.dat");
        try {
            return Files.exists(dataPath) ? ImmutableList.copyOf(Files.readAllLines(dataPath)) : ImmutableList.of();
        }
        catch (IOException e) {
            UIMessage.showErrorDialog(e, "Unable to load previous servers!");
            return ImmutableList.of();
        }
    }

    class LauncherButtonEvent implements ActionListener {

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
                    new IPAddressReceiver();
                    break;
            }
        }
    }

    class IPAddressReceiver extends JFrame {

        private JTextField ipField;

        IPAddressReceiver() {
            super("sync");
            setSize(new Dimension(300, 100));
            setResizable(false);
            setLayout(new GridLayout(3, 1));
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
                    dispose();
                    open();
                }
            });
            setLocationRelativeTo(null);

            JLabel label = new JLabel("Please enter or select the sync server you would like to join");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label);

            JPanel ipPanel = new JPanel(new GridLayout(1, 2));

            ipField = new JTextField();
            ipField.addActionListener(new IPAddressListener());
            ipPanel.add(ipField);
            JComboBox ipBox = new JComboBox(getPreviousAddresses().toArray());
            ipBox.setSelectedItem(null);
            ipBox.addItemListener(e -> {
                new Client(e.getItem().toString());
                dispose();
                setVisible(false);
            });
            ipPanel.add(ipBox);

            add(ipPanel);

            JButton connect = new JButton("Connect");
            connect.addActionListener(new IPAddressListener());
            add(connect);

            setVisible(true);
        }

        class IPAddressListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ipField.getText().isEmpty()) {
                    new Client(ipField.getText());
                    dispose();
                    setVisible(false);
                } else {
                    UIMessage.showMessageDialog(
                            "No IP address provided! An IP address must be provided!",
                            "Error with provided IP address!");
                }
            }
        }
    }
}
