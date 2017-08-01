package com.kirinpatel;

import com.google.common.collect.ImmutableList;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE;
import com.kirinpatel.net.*;
import com.kirinpatel.util.*;
import jdk.nashorn.api.scripting.URLReader;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.SERVER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Main class that will run the application and also server as the launcher for the application and primary object for
 * global variables.
 */
public class Main extends JFrame {

    private final static int VERSION = 1;
    private final static int BUILD = 6;
    private final static int REVISION = 0;

    // Global variables
    public static boolean showUserTimes = false;
    public static ArrayList<User> connectedUsers = new ArrayList<>();
    public static long deSyncWarningTime = 1000;
    public static long deSyncTime = 2000;

    private static Main main;
    private static JFrame frame;
    private static JTextField ipField;

    /**
     * Creates launcher window.
     */
    public Main() {
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

    /**
     * Main method.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (isUpdated()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e) {
                UIMessage.showErrorDialog(e, "Unable to set look and feel of sync.");
            } finally {
                if (verifyDependencies()) {
                    main = new Main();
                }
            }
        } else {
            UIMessage.showMessageDialog(
                    "You have an outdated version of sync, please update sync!",
                    "Outdated version of sync.");
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
                main.setVisible(true);
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
            main.dispose();
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

    private static boolean verifyDependencies() {
        if (!new NativeDiscovery().discover()) {
            UIMessage.showErrorDialog(new IllegalAccessException("Unable to load VLCJ." +
                    "\nPlease ensure that both VLC and Java are installed and use the same bit mode (32 or 64 bit)."),
                    "Unable to launch sync.");
            return false;
        }
        return true;
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

    private static boolean isUpdated() {
        try {
            Scanner s = new Scanner(new URLReader(new URL("https://github.com/ajchili/sync/releases")));
            String version = "";
            while (s.hasNext()) {
                String line = s.nextLine();
                if (line.contains("tag-reference")) {
                    s.nextLine();
                    version = s.nextLine();
                    version = version.substring(version.indexOf("tree/") + 5, version.indexOf("tree/") + 10);
                    break;
                }
            }

            int v = 0;
            int b = 0;
            int r = 0;

            for (int i = 0; i < 5; i += 2) {
                int parsedInt = Integer.parseInt(version.substring(i, i + 1));
                switch(i) {
                    case 0:
                        v = parsedInt;
                        break;
                    case 2:
                        b = parsedInt;
                        break;
                    case 4:
                        r = parsedInt;
                        break;
                    default:
                        break;
                }
            }

            return !(VERSION != v || BUILD < b || REVISION < r && BUILD == b);
        } catch(MalformedURLException e) {
            UIMessage.showErrorDialog(e, "Unable to verify version");
            return false;
        }
    }

    /**
     * Custom ActionListener that will serve to enable usability of Launcher
     * JButtons.
     */
    class LauncherButtonEvent implements ActionListener {

        private PANEL_TYPE type;

        /**
         * Main constructor that will establish the ActionListener with the
         * given type.
         *
         * @param type Type
         */
        LauncherButtonEvent(PANEL_TYPE type) {
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
                    dispose();
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
                main.dispose();
            } else {
                UIMessage.showMessageDialog(
                        "No IP address provided! An IP address must be provided!",
                        "Error with provided IP address!");
            }
        }
    }
}
