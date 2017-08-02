package com.kirinpatel.gui;

import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.net.User;
import com.kirinpatel.sync;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.SERVER;

public class ControlPanel extends JPanel {

    private final JList connectedClients;
    private final JScrollPane connectedClientsScroll;
    private final JPanel chatPanel;
    private final JTextArea chatWindow;
    private final JTextField chatField;
    private static ControlPanel INSTANCE;
    private static AtomicBoolean isInstanceSet = new AtomicBoolean(false);
    private GUI gui;
    static boolean isUserDisplayShown = false;
    int width = 300;

    static void setInstance(GUI gui) {
        if (isInstanceSet.compareAndSet(false, true)) {
           INSTANCE = new ControlPanel(gui);
        }
    }

    public static ControlPanel getInstance() {
        if (isInstanceSet.get()) {
            return INSTANCE;
        }
        throw new IllegalStateException("Control panel has not been set!");
    }

    private ControlPanel(GUI gui) {
        super(new GridLayout(2, 1));
        this.gui = gui;

        connectedClients = new JList();
        connectedClients.setToolTipText("Connected Clients");
        connectedClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectedClients.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (index < sync.connectedUsers.size()) {
                    final User host = sync.connectedUsers.get(0);
                    final User user = sync.connectedUsers.get(index);

                    if (host != null && !host.equals(user) && sync.showUserTimes) {
                        long currentUserTime = user.getMedia().getCurrentTime() + user.getPing();

                        if (host.getMedia().getCurrentTime() - sync.deSyncTime > currentUserTime) {
                            setBackground(Color.RED);
                        } else if (host.getMedia().getCurrentTime() - sync.deSyncWarningTime > currentUserTime) {
                            setBackground(Color.YELLOW);
                        }
                    }

                    if (!isUserDisplayShown && GUI.playbackPanel.type == SERVER && isSelected && cellHasFocus && index > 0) {
                        isUserDisplayShown = true;
                        chatWindow.requestFocus();
                        new ClientInfoGUI(user);
                    }
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
        JScrollPane chatWindowScroll = new JScrollPane(chatWindow);
        chatWindowScroll.setBorder(null);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        JPanel messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        chatField.addActionListener(new ControlPanel.SendMessageListener());
        messagePanel.add(chatField, BorderLayout.CENTER);
        JButton send = new JButton("Send");
        send.setInputMap(WHEN_FOCUSED, null);
        send.addActionListener(new ControlPanel.SendMessageListener());
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

    public void updateConnectedClients() {
        DefaultListModel listModel = new DefaultListModel();
        User host = sync.connectedUsers.get(0);
        for (User user : sync.connectedUsers) {
            String displayedText = user.toString();

            if (sync.showUserTimes) {
                displayedText += " (" + VLCJMediaPlayer.formatTime(user.getMedia().getCurrentTime()) + ')';
            }

            if (!user.equals(host)) {
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

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!chatField.getText().isEmpty()) {
                if (GUI.playbackPanel.type == SERVER) {
                    Server.sendMessage(sync.connectedUsers.get(0) + ": " + chatField.getText());
                    chatField.setText("");
                } else {
                    Client.sendMessage(Client.user.getUsername() + ": " + chatField.getText());
                    chatField.setText("");
                }
            }
        }
    }
}
