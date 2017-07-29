package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.User;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
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
    private GUI gui;
    public static boolean isUserDisplayShown = false;
    public static int width = 300;

    public ControlPanel(GUI gui, int type) {
        super(new GridLayout(2, 1));

        this.gui = gui;

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
                    long currentUserTime = user.getMedia().getCurrentTime() + user.getPing();

                    if (host.getMedia().getCurrentTime() - Main.deSyncTime > currentUserTime) {
                        setBackground(Color.RED);
                    } else if (host.getMedia().getCurrentTime() - Main.deSyncWarningTime > currentUserTime) {
                        setBackground(Color.YELLOW);
                    }
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
        /*
            Credit: https://stackoverflow.com/a/1627068
         */
        DefaultCaret caret = (DefaultCaret)chatWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatWindowScroll = new JScrollPane(chatWindow);
        chatWindowScroll.setBorder(null);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        JPanel messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        chatField.addActionListener(new ControlPanel.SendMessageListener(type));
        messagePanel.add(chatField, BorderLayout.CENTER);
        JButton send = new JButton("Send");
        send.setInputMap(WHEN_FOCUSED, null);
        send.addActionListener(new ControlPanel.SendMessageListener(type));
        messagePanel.add(send, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        add(chatPanel);
    }

    void resizePanel(int height) {
        gui.setMinimumSize(new Dimension(640 + width, 360));
        connectedClientsScroll.setPreferredSize(new Dimension(width, height / 2));
        chatPanel.setPreferredSize(new Dimension(width, height / 2));
        revalidate();
        repaint();
    }

    public void updateConnectedClients(ArrayList<User> users) {
        DefaultListModel listModel = new DefaultListModel();
        for (User user : users) {
            String displayedText = user.toString();

            if (Main.showUserTimes) {
                displayedText += " (" + VLCJMediaPlayer.formatTime(user.getMedia().getCurrentTime()) + ')';
            }

            if (!user.equals(users.get(0))) {
                displayedText += " (" + user.getPing() + " ms)";
            }

            listModel.addElement(displayedText);
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
    }

    public void addMessages(ArrayList<String> messages) {
        if (!chatWindow.getText().isEmpty()) {
            chatWindow.append("\n");
        }

        for (String message : messages) {
            chatWindow.append(message + '\n');
        }

        if (chatWindow.getText().length() > 0) {
            chatWindow.replaceRange("", chatWindow.getText().length() - 1, chatWindow.getText().length());
        }
    }

    class SendMessageListener implements ActionListener {

        private int type;

        SendMessageListener(int type) {
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
