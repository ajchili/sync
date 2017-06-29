package com.kirinpatel;

import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.UIMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Main class that will run the application. This class, as of version 2.0, will
 * also server as the launcher for the application.
 */
public class Main extends JFrame {

    public static boolean hideUI;
    public static double videoQuality = 1.0;
    private static Main main;
    private static String ipAddress = "";

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

        JPanel settingsPanel = new JPanel(new GridLayout(1, 3));
        JCheckBox hideUIBox = new JCheckBox("Hide UI (Client ONLY)");
        hideUIBox.addActionListener(e -> {
            hideUI = ((JCheckBox) e.getSource()).isSelected();
        });
        settingsPanel.add(hideUIBox);
        JComboBox qualityBox = new JComboBox();
        qualityBox.setModel(new DefaultComboBoxModel(new String[]{"Video Quality: 1.0", "Video Quality: 0.8", "Video Quality: 0.6", "Video Quality: 0.4", "Video Quality: 0.2"}));
        qualityBox.addActionListener(e -> {
            videoQuality = Double.parseDouble((((JComboBox) e.getSource()).getSelectedItem().toString()).replace("Video Quality: ", ""));
        });
        settingsPanel.add(qualityBox);
        add(settingsPanel, BorderLayout.SOUTH);

        setVisible(true);
        Debug.Log("Sync launcher displayed.", 3);
    }

    /**
     * Main method.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
        main = new Main();
    }

    private static void getIPAddress() {
        JFrame frame = new JFrame("sync");

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

        JTextField ipField = new JTextField();
        ipField.addActionListener(e -> {
            if (!ipField.getText().isEmpty()) {
                new Client(ipField.getText());
                frame.dispose();
                main.dispose();
            } else {
                new UIMessage("Error with provided IP address!", "No IP address provided! An IP address must be provided!", 1);
            }
        });
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
        connect.addActionListener(e -> {
            if (!ipField.getText().isEmpty()) {
                new Client(ipField.getText());
                frame.dispose();
                main.dispose();
            } else {
                new UIMessage("Error with provided IP address!", "No IP address provided! An IP address must be provided!", 1);
            }
        });
        frame.add(connect, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

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
}
