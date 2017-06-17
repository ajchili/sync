package com.kirinpatel.gui;

import com.kirinpatel.net.Server;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Kirin Patel
 * @version 0.0.1
 * @date 6/16/17
 */
public class ServerControlPanel extends JPanel {

    private JList connectedClients;
    private JScrollPane connectedClientsScroll;
    private JTextField urlField;
    private JButton setUrl;
    private JTextArea chatWindow;
    private JScrollPane chatWindowScroll;
    private JTextField chatField;
    private JButton send;

    public ServerControlPanel() {
        super();

        setLayout(new GridLayout(3, 1));

        connectedClients = new JList();
        connectedClients.setToolTipText("Connected Clients");
        connectedClients.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        connectedClientsScroll = new JScrollPane(connectedClients);
        add(connectedClientsScroll);

        JPanel mediaControlPanel = new JPanel(new GridLayout(2, 1));
        urlField = new JTextField();
        urlField.setToolTipText("Media URL");
        mediaControlPanel.add(urlField);
        setUrl = new JButton("Set Media URL");
        setUrl.addActionListener(e -> {
            if (!urlField.getText().isEmpty() && urlField.getText().endsWith(".mp4")) {
                ServerGUI.mediaPanel.setMediaURL(urlField.getText());
            } else if (urlField.getText().isEmpty()) {
                if (!ServerGUI.mediaPanel.getMediaURL().isEmpty()) {
                    ServerGUI.mediaPanel.setMediaURL("");
                } else {
                    new UIMessage("Error setting Media URL!", "The Media URL must be specified!", 1);
                }
            } else if (!urlField.getText().endsWith(".mp4")) {
                new UIMessage("Error setting Media URL!", "The Media URL must be of .mp4 format!", 1);
            }
        });
        mediaControlPanel.add(setUrl);
        add(mediaControlPanel);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        chatWindow.setToolTipText("Chat Box");
        chatWindowScroll = new JScrollPane(chatWindow);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        JPanel messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        messagePanel.add(chatField, BorderLayout.CENTER);
        send = new JButton("Send");
        messagePanel.add(send, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        add(chatPanel);
    }

    public void resizePanel(int width, int height) {
        width = ((width - (height * 16 / 9)) < 200) ? 200 : width - (height * 16 / 9);
        connectedClientsScroll.setPreferredSize(new Dimension(width, height / 3));
        urlField.setPreferredSize(new Dimension(width, height / 6));
        setUrl.setPreferredSize(new Dimension(width, height / 6));
    }

    public void updateConnectedClients(ArrayList<User> users) {
        DefaultListModel listModel = new DefaultListModel();
        for (User user : users) {
            listModel.addElement(user);
        }
        connectedClients.setModel(listModel);
    }

}
