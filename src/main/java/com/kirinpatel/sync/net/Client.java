package com.kirinpatel.sync.net;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;

import com.kirinpatel.sync.Launcher;
import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.gui.ControlPanel;
import com.kirinpatel.sync.gui.GUI;
import com.kirinpatel.sync.utils.Message;
import com.kirinpatel.sync.utils.UIMessage;
import com.kirinpatel.sync.utils.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    public static String ipAddress;
    public static User user;
    private static ClientThread clientThread;
    private static ArrayList<String> messages = new ArrayList<>();
    private boolean isRunning = false;
    private boolean isServerClosed = false;
    private Socket socket;
    public static GUI gui;

    public Client(String ipAddress) {
        Client.ipAddress = ipAddress;
        Client.user = new User(System.getProperty("user.name"));
        clientThread = new ClientThread();
        messages.clear();
        new Thread(clientThread).start();
    }

    public static void stop() {
        GUI.playbackPanel.getMediaPlayer().release();
        gui.dispose();
        clientThread.stop();
    }

    public static void sendMessage(String message) {
        messages.add(message);
    }

    class ClientThread implements Runnable {

        private ObjectInputStream input;
        private ObjectOutputStream output;
        private boolean isConnected = false;
        private long lastSentTime = 0;
        private long lastSentState = 0;

        public void run() {
            isRunning = true;

            connectToServer();

            while(isConnected && isRunning) {
                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch(message.getType()) {
                            case CLOSING:
                                isServerClosed = true;
                                gui.hide();
                                break;
                            case PING:
                                sendMessage(new Message.Builder(Message.MESSAGE_TYPE.PING).build());
                                break;
                            case CONNECTED_CLIENTS:
                                Sync.connectedUsers = (ArrayList<User>) message.getBody();
                                Sync.host = Sync.connectedUsers.get(0);
                                ControlPanel.getInstance().updateConnectedClients();
                                break;
                            case MEDIA_URL:
                                String mediaURL = (String) message.getBody();
                                if (!mediaURL.equals("") && !GUI.playbackPanel.getMedia().getURL().equals(mediaURL)) {
                                    lastSentTime = 0;
                                    GUI.playbackPanel.getMediaPlayer().setMedia(new Media(mediaURL));
                                }
                                sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_URL)
                                        .body(GUI.playbackPanel.getMedia().getURL())
                                        .build());
                                break;
                            case MEDIA_TIME:
                                long time = (long) message.getBody();
                                if (!GUI.playbackPanel.getMedia().isPaused()) {
                                    GUI.playbackPanel.getMediaPlayer().seekTo(time);
                                }
                                break;
                            case MEDIA_RATE:
                                float rate = (float) message.getBody();
                                if (!GUI.playbackPanel.getMedia().isPaused()) {
                                    GUI.playbackPanel.getMediaPlayer().setRate(rate);
                                }
                                break;
                            case MEDIA_STATE:
                                boolean isServerPaused = (boolean) message.getBody();
                                if (isServerPaused != GUI.playbackPanel.getMediaPlayer().isPaused()) {
                                    if (isServerPaused) {
                                        GUI.playbackPanel.getMediaPlayer().pause();
                                    } else {
                                        GUI.playbackPanel.getMediaPlayer().play();
                                    }
                                }
                                break;
                            case MESSAGES:
                                ControlPanel.getInstance().addMessages((ArrayList<String>) message.getBody());
                                break;
                            default:
                                break;
                        }
                    }
                } catch(IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (!messages.isEmpty()) {
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MESSAGES)
                            .body(messages)
                            .build());
                }

                if (System.currentTimeMillis() > lastSentTime + 250) {
                    lastSentTime = System.currentTimeMillis();
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_TIME)
                            .body(GUI.playbackPanel.getMedia().getCurrentTime())
                            .build());
                }

                if (System.currentTimeMillis() > lastSentState + 500) {
                    lastSentState = System.currentTimeMillis();
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_STATE)
                            .body(GUI.playbackPanel.getMedia().isPaused())
                            .build());
                }
            }

            disconnectFromServer();
        }

        void stop() {
            isRunning = false;

            isConnected = false;
        }

        private synchronized void connectToServer() {
            try {
                socket = new Socket(Client.ipAddress, 8000);
                socket.setKeepAlive(true);
            } catch(IOException e) {
                UIMessage.showErrorDialog(e, "Couldn't connect to server!");
                isConnected = false;
                return;
            }

            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message.Builder(Message.MESSAGE_TYPE.CONNECTING).build());
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) input.readObject();
                isConnected = message.getType() == Message.MESSAGE_TYPE.CONNECTED;
            } catch(IOException | ClassNotFoundException e) {
                disconnectFromServer();
            }

            gui = new GUI(CLIENT);
            Launcher.saveIPAddress(ipAddress);

            sendMessage(new Message.Builder(Message.MESSAGE_TYPE.CLIENT_NAME).body(user.getUsername()).build());
        }

        private synchronized void disconnectFromServer() {
            try {
                if (output != null && !isServerClosed) {
                    isConnected = false;
                    output.writeObject(new Message.Builder(Message.MESSAGE_TYPE.DISCONNECTING).build());
                    output.flush();
                } else if (isServerClosed) {
                    UIMessage.showMessageDialog(
                            "The sync server that you were connected to has shutdown.",
                            "Server shut down.");
                }

                if (socket != null) {
                    socket.close();
                }
            } catch(IOException e) {
                Client.stop();
            } finally {
                Launcher.INSTANCE.open();
            }
        }

        private synchronized void sendMessage(Message message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                disconnectFromServer();
            }
        }
    }
}
