package com.kirinpatel.gui;

import com.kirinpatel.net.Client;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Kirin Patel
 * @version 0.0.4
 * @date 6/17/17
 */
public class ClientControlPanel extends JPanel {

    private JList connectedClients;
    private JScrollPane connectedClientsScroll;
    private JTextArea chatWindow;
    private JScrollPane chatWindowScroll;
    private JTextField chatField;
    private JButton send;

    public ClientControlPanel() {
        super(new GridLayout(3, 1));

        Debug.Log("Creating ClientControlPanel.", 3);

        connectedClients = new JList();
        connectedClients.setToolTipText("Connected Clients");
        connectedClients.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        connectedClientsScroll = new JScrollPane(connectedClients);
        add(connectedClientsScroll);

        add(new JPanel());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        chatWindow.setToolTipText("Chat Box");
        chatWindowScroll = new JScrollPane(chatWindow);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        JPanel messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        chatField.addActionListener(new SendMessageListener());
        messagePanel.add(chatField, BorderLayout.CENTER);
        send = new JButton("Send");
        send.addActionListener(new SendMessageListener());
        messagePanel.add(send, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        add(chatPanel);

        Debug.Log("ClientControlPanel created.", 3);
    }

    public void resizePanel(int width, int height) {
        width = ((width - (height * 16 / 9)) < 200) ? 200 : width - (height * 16 / 9);
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
        if (chatWindow.getText().length() != 0) {
            chatWindow.append("\n");
        }
        for (String message : messages) {
            chatWindow.append(message + "\n");
        }
        chatWindow.replaceRange("", chatWindow.getText().length() - 1, chatWindow.getText().length());
    }

    class SendMessageListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!chatField.getText().isEmpty()) {
                Client.sendMessage(Client.user.getUsername() + ": " + chatField.getText());
                chatField.setText("");
            }
        }
    }
}
