package com.kirinpatel.net;

import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.util.Message.MESSAGE_TYPE.*;

import com.kirinpatel.Launcher;
import com.kirinpatel.Sync;
import com.kirinpatel.gui.ControlPanel;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.util.Message;
import com.kirinpatel.util.UIMessage;

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
        if (gui.isVisible()) {
            gui.hide();
        }
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
                                sendPing();
                                break;
                            case CONNECTED_CLIENTS:
                                Sync.connectedUsers = (ArrayList<User>) message.getMessage();
                                Sync.host = Sync.connectedUsers.get(0);
                                ControlPanel.getInstance().updateConnectedClients();
                                break;
                            case MEDIA_URL:
                                String mediaURL = (String) message.getMessage();
                                if (!mediaURL.equals("") && !GUI.playbackPanel.getMedia().getURL().equals(mediaURL)) {
                                    lastSentTime = 0;
                                    GUI.playbackPanel.getMediaPlayer().setMedia(new Media(mediaURL));
                                }
                                sendMediaURL();
                                break;
                            case MEDIA_TIME:
                                long time = (long) message.getMessage();
                                if (!GUI.playbackPanel.getMedia().isPaused()) {
                                    GUI.playbackPanel.getMediaPlayer().seekTo(time);
                                }
                                break;
                            case MEDIA_RATE:
                                float rate = (float) message.getMessage();
                                if (!GUI.playbackPanel.getMedia().isPaused()) {
                                    GUI.playbackPanel.getMediaPlayer().setRate(rate);
                                }
                                break;
                            case MEDIA_STATE:
                                boolean isServerPaused = (boolean) message.getMessage();
                                if (isServerPaused != GUI.playbackPanel.getMediaPlayer().isPaused()) {
                                    if (isServerPaused) {
                                        GUI.playbackPanel.getMediaPlayer().pause();
                                    } else {
                                        GUI.playbackPanel.getMediaPlayer().play();
                                    }
                                }
                                break;
                            case MESSAGES:
                                ControlPanel.getInstance().addMessages((ArrayList<String>) message.getMessage());
                                break;
                            default:
                                break;
                        }
                    }
                } catch(IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (!messages.isEmpty()) {
                    sendMessages();
                }

                if (System.currentTimeMillis() > lastSentTime + 250) {
                    sendMediaTime();
                }

                if (System.currentTimeMillis() > lastSentState + 500) {
                    sendMediaState();
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
                output.writeObject(new Message(CONNECTING, ""));
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) input.readObject();
                isConnected = message.getType() == CONNECTED;
            } catch(IOException | ClassNotFoundException e) {
                disconnectFromServer();
            }

            gui = new GUI(CLIENT);
            Launcher.saveIPAddress(ipAddress);

            sendUsernameToServer();
        }

        private synchronized void disconnectFromServer() {
            try {
                if (output != null && !isServerClosed) {
                    isConnected = false;
                    output.writeObject(new Message(DISCONNECTING, null));
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

        private synchronized void sendPing() {
            try {
                output.writeObject(new Message(PING, null));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendUsernameToServer() {
            try {
                output.reset();
                output.writeObject(new Message(CLIENT_NAME, user.getUsername()));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendMessages() {
            try {
                output.reset();
                output.writeObject(new Message(CLIENT_MESSAGES, messages));
                output.flush();
                messages.clear();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendMediaURL() {
            try {
                output.flush();
                output.writeObject(new Message(MEDIA_URL, GUI.playbackPanel.getMedia().getURL()));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendMediaTime() {
            try {
                lastSentTime = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(MEDIA_TIME, GUI.playbackPanel.getMedia().getCurrentTime()));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendMediaState() {
            try {
                lastSentState = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(MEDIA_STATE, GUI.playbackPanel.getMedia().isPaused()));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }
    }
}
