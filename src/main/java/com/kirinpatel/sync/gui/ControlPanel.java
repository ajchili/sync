package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.Launcher;
import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.net.User;
import com.kirinpatel.sync.util.Theme;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

public class ControlPanel extends JPanel {

    static long deSyncWarningTime = 1000L;
    static long deSyncTime = 5000L;
    public static boolean showUserTimes = false;
    private final JList connectedClients;
    private final JScrollPane connectedClientsScroll;
    private final JPanel chatPanel;
    private final JPanel messagePanel;
    private final JTextArea chatWindow;
    private final JTextField chatField;
    private final JButton send;
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

                if (index < Sync.connectedUsers.size()) {
                    final User host = Sync.host;
                    final User user = Sync.connectedUsers.get(index);

                    if (host != null && !host.equals(user) && showUserTimes) {
                        long currentUserTime = user.getMedia().currentTime + user.getPing();

                        if (host.getMedia().currentTime - deSyncTime > currentUserTime) {
                            setBackground(Color.RED);
                        } else if (host.getMedia().currentTime - deSyncWarningTime > currentUserTime) {
                            setBackground(Color.YELLOW);
                        }
                    }

                    if (!isUserDisplayShown && gui.playbackPanel.type == SERVER && isSelected && cellHasFocus && index > 0) {
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
        // Credit: https://stackoverflow.com/a/1627068
        DefaultCaret caret = (DefaultCaret)chatWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane chatWindowScroll = new JScrollPane(chatWindow);
        chatWindowScroll.setBorder(null);
        chatPanel.add(chatWindowScroll, BorderLayout.CENTER);
        messagePanel = new JPanel(new BorderLayout());
        chatField = new JTextField();
        chatField.setToolTipText("Message Box");
        chatField.addActionListener(new ControlPanel.SendMessageListener());
        messagePanel.add(chatField, BorderLayout.CENTER);
        send = new JButton("Send");
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

    void setWidth() {
        int height = getHeight();
        gui.setMinimumSize(new Dimension(640 + width, 360));
        gui.setSize(new Dimension(640 + width, 360));
        connectedClientsScroll.setPreferredSize(new Dimension(width, height / 2));
        chatPanel.setPreferredSize(new Dimension(width, height / 2));
        revalidate();
        repaint();
    }

    public void updateConnectedClients() {
        DefaultListModel listModel = new DefaultListModel();
        User host = Sync.host;
        for (User user : Sync.connectedUsers) {
            String displayedText = user.toString();

            if (showUserTimes) {
                displayedText += " (" + user.getMedia().getFormattedTime() + ')';
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

    void setUIMode() {
        connectedClients.setForeground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getTEXT_DARK()
                : Theme.Companion.getTEXT_STANDARD());
        connectedClients.setBackground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getBACKGROUND_DARK()
                : Theme.Companion.getBACKGROUND_STANDARD());
        chatWindow.setForeground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getTEXT_DARK()
                : Theme.Companion.getTEXT_STANDARD());
        chatWindow.setBackground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getBACKGROUND_DARK()
                : Theme.Companion.getBACKGROUND_STANDARD());
        messagePanel.setBackground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getBACKGROUND_DARK()
                : Theme.Companion.getBACKGROUND_STANDARD());
        chatField.setForeground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getTEXT_DARK()
                : Theme.Companion.getTEXT_STANDARD());
        chatField.setBackground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getBACKGROUND_DARK()
                : Theme.Companion.getBACKGROUND_STANDARD());
        send.setForeground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getTEXT_DARK()
                : Theme.Companion.getTEXT_STANDARD());
        send.setBackground(Theme.Companion.isDarkModeEnabled()
                ? Theme.Companion.getBACKGROUND_DARK()
                : Theme.Companion.getBACKGROUND_STANDARD());
        repaint();
    }

    class SendMessageListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!chatField.getText().isEmpty()) {
                Launcher.INSTANCE.connectedUser.sendMessage(
                        Launcher.INSTANCE.connectedUser.getUser().getUsername() + ": " + chatField.getText());
                chatField.setText("");
            }
        }
    }
}
