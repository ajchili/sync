package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Kirin Patel
 * @version 0.0.3
 * @date 6/21/17
 */
public class ControlPanel extends JPanel {

    private JList connectedClients;
    private JScrollPane connectedClientsScroll;
    private JPanel mediaControlPanel;
    private JTextField urlField;
    private JButton setUrl;
    private JPanel chatPanel;
    private JPanel messagePanel;
    private JTextArea chatWindow;
    private JScrollPane chatWindowScroll;
    private JTextField chatField;
    private JButton send;

    public ControlPanel(int type) {
        super(new GridLayout(3, 1));

        Debug.Log("Creating ControlPanel...", 3);

        connectedClients = new JList();
        connectedClients.setToolTipText("Connected Clients");
        connectedClients.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        connectedClientsScroll = new JScrollPane(connectedClients);
        connectedClientsScroll.setBorder(null);
        add(connectedClientsScroll);

        if (type == 0) {
            mediaControlPanel = new JPanel(new GridLayout(2, 1));
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
                        Debug.Log("Media URL not specified!", 2);
                        new UIMessage("Error setting Media URL!", "The Media URL must be specified!", 1);
                    }
                } else if (!urlField.getText().endsWith(".mp4")) {
                    Debug.Log("Media URL is not .mp4 format!", 2);
                    new UIMessage("Error setting Media URL!", "The Media URL must be of .mp4 format!", 1);
                }
            });
            mediaControlPanel.add(setUrl);
            add(mediaControlPanel);
        } else {
            add(new JPanel());
        }

        chatPanel = new JPanel(new BorderLayout());
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        chatWindow.setToolTipText("Chat Box");
        chatWindowScroll = new JScrollPane(chatWindow);
        chatWindowScroll.setBorder(null);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        chatField.addActionListener(new ControlPanel.SendMessageListener(type));
        messagePanel.add(chatField, BorderLayout.CENTER);
        send = new JButton("Send");
        send.addActionListener(new ControlPanel.SendMessageListener(type));
        messagePanel.add(send, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        add(chatPanel);

        if (Main.isDarkModeEnabled) {
            Color foreground = getBackground();
            setBackground(Color.black);
            connectedClients.setBackground(Color.black);
            connectedClients.setForeground(foreground);
            mediaControlPanel.setBackground(Color.black);
            urlField.setBackground(Color.black);
            urlField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 2, 7, 2), BorderFactory.createLineBorder(foreground, 3, true)));
            urlField.setForeground(foreground);
            setUrl.setBackground(Color.black);
            setUrl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createLineBorder(foreground, 3, true)));
            setUrl.setForeground(foreground);
            chatPanel.setBackground(Color.black);
            messagePanel.setBackground(Color.black);
            chatWindow.setBackground(Color.black);
            chatWindow.setForeground(foreground);
            chatField.setBackground(Color.black);
            chatField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 2, 7, 2), BorderFactory.createLineBorder(foreground, 3, true)));
            chatField.setForeground(foreground);
            send.setBackground(Color.black);
            send.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createLineBorder(foreground, 3, true)));
            send.setForeground(foreground);
        }

        Debug.Log("ControlPanel created.", 3);
    }

    public void resizePanel(int width, int height) {
        width = ((width - (height * 16 / 9)) < 200) ? 200 : width - (height * 16 / 9);
        if (urlField != null) urlField.setPreferredSize(new Dimension(width, height / 3));
        connectedClientsScroll.setPreferredSize(new Dimension(width, height / 3));
    }

    public void updateConnectedClients(ArrayList<User> users) {
        Debug.Log("Updating connected clients in gui...", 3);
        DefaultListModel listModel = new DefaultListModel();
        for (User user : users) {
            listModel.addElement(user);
        }
        connectedClients.setModel(listModel);
        Debug.Log("Connected clients updated in gui.", 3);
    }

    public void setMessages(ArrayList<String> messages) {
        chatWindow.setText("");
        for (String message : messages) {
            if (messages.indexOf(message) != messages.size() - 1) {
                chatWindow.append(message + "\n");
            } else {
                chatWindow.append(message);
            }
        }
        chatWindowScroll.getVerticalScrollBar().setValue(chatWindowScroll.getVerticalScrollBar().getMaximum());
    }

    public void addMessages(ArrayList<String> messages) {
        if (chatWindow.getText().length() != 0) {
            chatWindow.append("\n");
        }
        for (String message : messages) {
            chatWindow.append(message + "\n");
        }
        chatWindow.replaceRange("", chatWindow.getText().length() - 1, chatWindow.getText().length());
        chatWindowScroll.getVerticalScrollBar().setValue(chatWindowScroll.getVerticalScrollBar().getMaximum());
    }

    class SendMessageListener implements ActionListener {

        private int type;

        public SendMessageListener(int type) {
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!chatField.getText().isEmpty()) {
                if (type == 0) {
                    Server.sendMessage(Server.connectedClients.get(0) + ": " + chatField.getText());
                    chatField.setText("");
                } else {
                    Client.sendMessage(Client.user.getUsername() + ": " + chatField.getText());
                    chatField.setText("");
                }
            }
        }
    }
}
