package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.User;
import com.kirinpatel.vlc.VLCJMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ControlPanel extends JPanel {

    private JList connectedClients;
    private JScrollPane connectedClientsScroll;
    private JPanel chatPanel;
    private JTextArea chatWindow;
    private JScrollPane chatWindowScroll;
    private JTextField chatField;
    public static boolean isUserDisplayShown = false;

    public ControlPanel(int type) {
        super(new GridLayout(2, 1));

        connectedClients = new JList();
        connectedClients.setToolTipText("Connected Clients");
        connectedClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectedClients.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                User host = Main.connectedUsers.get(0);
                User user = Main.connectedUsers.get(index);

                if (host != null && !host.equals(user) && Main.showUserTimes) {
                    if (host.getTime() - Main.deSyncTime > user.getTime()) setBackground(Color.RED);
                    else if (host.getTime() - Main.deSyncWarningTime > user.getTime()) setBackground(Color.YELLOW);
                }

                if (!isUserDisplayShown && type == 0 && isSelected && cellHasFocus && index > 0) {
                    isUserDisplayShown = true;
                    chatWindow.requestFocus();
                    new ClientInfoGUI(index);
                }

                return c;
            }
        });
        connectedClientsScroll = new JScrollPane(connectedClients);
        connectedClientsScroll.setBorder(null);
        add(connectedClientsScroll);

        chatPanel = new JPanel(new BorderLayout());
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        chatWindow.setLineWrap(true);
        chatWindow.setWrapStyleWord(true);
        chatWindow.setToolTipText("Chat Box");
        chatWindowScroll = new JScrollPane(chatWindow);
        chatWindowScroll.setBorder(null);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        JPanel messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        chatField.addActionListener(new ControlPanel.SendMessageListener(type));
        messagePanel.add(chatField, BorderLayout.CENTER);
        JButton send = new JButton("Send");
        send.setInputMap(0, null);
        send.addActionListener(new ControlPanel.SendMessageListener(type));
        messagePanel.add(send, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        add(chatPanel);
    }

    public void resizePanel(int height) {
        connectedClientsScroll.setPreferredSize(new Dimension(200, height / 2));
        chatPanel.setPreferredSize(new Dimension(200, height / 2));
    }

    public void updateConnectedClients(ArrayList<User> users) {
        DefaultListModel listModel = new DefaultListModel();
        for (User user : users) {
            if (Main.showUserTimes) listModel.addElement(user + " (" + VLCJMediaPlayer.formatTime(user.getTime()) + ')');
            else listModel.addElement(user);
        }
        connectedClients.setModel(listModel);
    }

    public void setMessages(ArrayList<String> messages) {
        chatWindow.setText("");
        for (String message : messages) {
            if (messages.indexOf(message) != messages.size() - 1) {
                chatWindow.append(message + '\n');
            } else {
                chatWindow.append(message);
            }
        }
        chatWindowScroll.getVerticalScrollBar().setValue(chatWindowScroll.getVerticalScrollBar().getMaximum());
    }

    public void addMessages(ArrayList<String> messages) {
        if (!chatWindow.getText().isEmpty()) {
            chatWindow.append("\n");
        }
        for (String message : messages) {
            chatWindow.append(message + '\n');
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
                    Server.sendMessage(Main.connectedUsers.get(0) + ": " + chatField.getText());
                    chatField.setText("");
                } else {
                    Client.sendMessage(Client.user.getUsername() + ": " + chatField.getText());
                    chatField.setText("");
                }
            }
        }
    }
}
