package com.kirinpatel;

import com.google.common.collect.ImmutableList;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.net.User;
import com.kirinpatel.util.UIMessage;
import jdk.nashorn.api.scripting.URLReader;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.SERVER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Launcher extends JFrame {

    private final static int VERSION = 1;
    private final static int BUILD = 6;
    private final static int REVISION = 0;

    // Global variables
    public static boolean showUserTimes = false;
    public static ArrayList<User> connectedUsers = new ArrayList<>();
    public static long deSyncWarningTime = 1000;
    public static long deSyncTime = 2000;

    private static JFrame frame;
    private static JTextField ipField;

    private static Launcher INSTANCE;
    private static AtomicBoolean isInstanceSet = new AtomicBoolean(false);

    static void setInstance() {
        if (isInstanceSet.compareAndSet(false, true)) {
            if (isUpdated()) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch(Exception e) {
                    UIMessage.showErrorDialog(e, "Unable to set look and feel of sync.");
                } finally {
                    if (verifyDependencies()) {
                        INSTANCE = new Launcher();
                    }
                }
            } else {
                UIMessage.showMessageDialog(
                        "You have an outdated version of sync, please update sync!",
                        "Outdated version of sync.");
            }
        }
    }

    public static Launcher getInstance() {
        if (isInstanceSet.get()) {
            return INSTANCE;
        }
        throw new IllegalStateException("Control panel has not been set!");
    }

    Launcher() {
        super("sync");

        connectedUsers.clear();
        showUserTimes = false;

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
    }

    private static boolean verifyDependencies() {
        if (!new NativeDiscovery().discover()) {
            UIMessage.showErrorDialog(new IllegalAccessException("Unable to load VLCJ." +
                            "\nPlease ensure that both VLC and Java are installed and use the same bit mode (32 or 64 bit)."),
                    "Unable to launch sync.");
            return false;
        }
        return true;
    }

    private static boolean isUpdated() {
        try {
            Scanner s = new Scanner(new URLReader(new URL("https://github.com/ajchili/sync/releases")));
            String version = "";
            while(s.hasNext()) {
                String line = s.nextLine();
                if (line.contains("tag-reference")) {
                    s.nextLine();
                    version = s.nextLine();
                    version = version.substring(version.indexOf("tree/") + 5, version.indexOf("tree/") + 10);
                    break;
                }
            }

            int v = Integer.parseInt(version.substring(0, version.indexOf('.')));
            int b = Integer.parseInt(version.substring(version.indexOf('.') + 1, version.lastIndexOf('.')));
            int r = Integer.parseInt(version.substring(version.lastIndexOf('.') + 1));

            return !(VERSION != v || BUILD < b || REVISION < r && BUILD == b);
        } catch(MalformedURLException e) {
            UIMessage.showErrorDialog(e, "Unable to verify version");
            return false;
        }
    }

    /**
     * Gets desired server IP address that the client would like to connect to.
     */
    private static void getIPAddress() {
        frame = new JFrame("sync");

        frame.setSize(new Dimension(300, 100));
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
        frame.addComponentListener(new ComponentListener() {
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
                frame.dispose();
                Launcher.getInstance().setVisible(false);
            }
        });
        frame.setLocationRelativeTo(null);

        JLabel label = new JLabel("Please enter or select the sync server you would like to join");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(label, BorderLayout.NORTH);

        JPanel ipPanel = new JPanel(new GridLayout(1, 2));

        ipField = new JTextField();
        ipField.addActionListener(new IPAddressListener());
        ipPanel.add(ipField);
        JComboBox ipBox = new JComboBox(getPreviousAddresses().toArray());
        ipBox.setSelectedItem(null);
        ipBox.addItemListener(e -> {
            new Client(e.getItem().toString());
            frame.dispose();
            Launcher.getInstance().setVisible(false);
        });
        ipPanel.add(ipBox);

        frame.add(ipPanel, BorderLayout.CENTER);

        JButton connect = new JButton("Connect");
        connect.addActionListener(new IPAddressListener());
        frame.add(connect, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    /**
     * Saves IP address to file for easier connections to the server in the future.
     *
     * @param ipAddress Server IP address
     */
    public static void saveIPAddress(String ipAddress) {
        if (getPreviousAddresses().contains(ipAddress)) {
            return;
        }
        Path dataPath = Paths.get("launcherData.dat");
        try {
            Files.write(
                    dataPath,
                    Arrays.asList(ipAddress),
                    UTF_8,
                    Files.exists(dataPath) ? APPEND : CREATE);
        } catch (IOException e) {
            UIMessage.showErrorDialog(e, "Couldn't save IP address!");
        }
    }

    /**
     * Returns previously connected sync server IP addresses.
     *
     * @return ArrayList of previous server IP addresses
     */
    private static ImmutableList<String> getPreviousAddresses() {
        Path dataPath = Paths.get("launcherData.dat");
        try {
            return Files.exists(dataPath) ? ImmutableList.copyOf(Files.readAllLines(dataPath)) : ImmutableList.of();
        }
        catch (IOException e) {
            UIMessage.showErrorDialog(e, "Unable to load previous servers");
            return ImmutableList.of();
        }
    }

    /**
     * Custom ActionListener that will serve to enable usability of Launcher
     * JButtons.
     */
    class LauncherButtonEvent implements ActionListener {

        private PlaybackPanel.PANEL_TYPE type;

        /**
         * Main constructor that will establish the ActionListener with the
         * given type.
         *
         * @param type Type
         */
        LauncherButtonEvent(PlaybackPanel.PANEL_TYPE type) {
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
                case SERVER:
                    new Server();
                    Launcher.getInstance().setVisible(false);
                    break;
                case CLIENT:
                    getIPAddress();
                    break;
            }
        }
    }

    /**
     * Custom ActionListener that will serve to enable usability of the IP address field during Client connections.
     */
    static class IPAddressListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!ipField.getText().isEmpty()) {
                new Client(ipField.getText());
                frame.dispose();
                Launcher.getInstance().setVisible(false);
            } else {
                UIMessage.showMessageDialog(
                        "No IP address provided! An IP address must be provided!",
                        "Error with provided IP address!");
            }
        }
    }
}
