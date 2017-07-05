package com.kirinpatel;

import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.VersionChecker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Main class that will run the application and also server as the launcher for the application.
 */
public class Main extends JFrame {

    public static int videoQuality = 100;
    private static Main main;
    private static JFrame frame;
    private static JTextField ipField;

    /**
     * Creates launcher window.
     */
    public Main() {
        super("sync");

        Debug.Log("Starting sync launcher...", 3);
        setSize(new Dimension(300, 150));

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

        setVisible(true);
        Debug.Log("Sync launcher displayed.", 3);
    }

    /**
     * Main method.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (VersionChecker.isUpdated()) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                Debug.Log("Unable to load \"Numbus\" UIManager!", 2);
            }
            main = new Main();
        } else {
            Debug.Log("Outdated version of sync! Please update!", 2);
            new UIMessage("Outdated version of sync", "You have an outdated version of sync, please update sync!", 1);
        }
    }

    /**
     * Gets desired server IP address that the client would like to connect to.
     */
    private static void getIPAddress() {
        frame = new JFrame("sync");

        frame.setSize(new Dimension(350, 100));
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
        JComboBox ipBox;
        if (getPreviousAddresses() != null) ipBox = new JComboBox(getPreviousAddresses().toArray());
        else ipBox = new JComboBox();
        ipBox.setSelectedItem(null);
        ipBox.addItemListener(e -> {
            ipField.setText(e.getItem().toString());
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
        if (getPreviousAddresses() != null) {
            for (String ip : getPreviousAddresses()) {
                if (ip.equals(ipAddress)) return;
            }
        }

        File file = new File("launcherData.dat");
        BufferedWriter writer;
        if (!file.exists()) {
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(ipAddress);

                writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                writer = new BufferedWriter(new FileWriter(file, true));
                writer.append('\n');
                writer.append(ipAddress);

                writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns previously connected sync server IP addresses.
     *
     * @return ArrayList of previous server IP addresses
     */
    private static ArrayList<String> getPreviousAddresses() {
        File file = new File("launcherData.dat");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String ipAddress;
                ArrayList<String> ipAddresses = new ArrayList<>();

                while ((ipAddress = reader.readLine()) != null) {
                    ipAddresses.add(ipAddress);
                }

                return ipAddresses;
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
                new UIMessage("Error with provided IP address!", "No IP address provided! An IP address must be provided!", 1);
            }
        }
    }
}
