package com.kirinpatel.sync.net;

import com.kirinpatel.sync.Launcher;
import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.gui.ControlPanel;
import com.kirinpatel.sync.gui.GUI;
import com.kirinpatel.sync.util.Message;
import com.kirinpatel.sync.util.UIMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.sync.util.Message.MESSAGE_TYPE.*;

public class Client implements NetworkUser {

    public static String ipAddress;
    public static User user;
    private static ClientThread clientThread;
    private static ArrayList<String> messages = new ArrayList<>();
    private boolean isRunning = false;
    private boolean isServerClosed = false;
    private Socket socket;
    public static GUI gui;

    public Client(String ipAddress) {
        Launcher.INSTANCE.connectedUser = this;
        Client.ipAddress = ipAddress;
        Client.user = new User(System.getProperty("user.name"));
        clientThread = new ClientThread();
        messages.clear();
        new Thread(clientThread).start();
    }

    @Override
    public void stop() {
        gui.playbackPanel.getMediaPlayer().release();
        gui.dispose();
        clientThread.stop();
    }
    @Override
    public void sendMessage(String message) {
        messages.add(message);
    }

    @Override
    public User getUser() {
        return user;
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
                        switch(message.getMessageType()) {
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
                                if (!mediaURL.equals("") && !gui.playbackPanel.getMedia().getURL().equals(mediaURL)) {
                                    lastSentTime = 0;
                                    gui.playbackPanel.getMediaPlayer().setMedia(new Media(mediaURL));
                                }
                                sendMediaURL();
                                break;
                            case MEDIA_TIME:
                                long time = (long) message.getMessage();
                                if (!gui.playbackPanel.getMedia().isPaused()) {
                                    gui.playbackPanel.getMediaPlayer().seekTo(time);
                                }
                                break;
                            case MEDIA_RATE:
                                float rate = (float) message.getMessage();
                                if (!gui.playbackPanel.getMedia().isPaused()) {
                                    gui.playbackPanel.getMediaPlayer().setRate(rate);
                                }
                                break;
                            case MEDIA_STATE:
                                boolean isServerPaused = (boolean) message.getMessage();
                                if (isServerPaused != gui.playbackPanel.getMediaPlayer().isPaused()) {
                                    if (isServerPaused) {
                                        gui.playbackPanel.getMediaPlayer().pause();
                                    } else {
                                        gui.playbackPanel.getMediaPlayer().play();
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
                isConnected = message.getMessageType() == CONNECTED;
            } catch(IOException | ClassNotFoundException e) {
                UIMessage.showErrorDialog(e, "Couldn't connect to server!");
                disconnectFromServer();
            }

            gui = new GUI(CLIENT);
            Launcher.saveIPAddress(ipAddress);

            sendUsernameToServer();
        }

        private synchronized void disconnectFromServer() {
            try {
                if (!isConnected) {
                    return;
                }
                isConnected = false;
                if (output != null && !isServerClosed) {
                    output.writeObject(new Message(DISCONNECTING, ""));
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
                Client.this.stop();
            } finally {
                Launcher.INSTANCE.open();
            }
        }

        private synchronized void sendPing() {
            try {
                output.writeObject(new Message(PING, ""));
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
                UIMessage.showErrorDialog(e, "Couldn't connect to server!");
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
                output.reset();
                Message message = new Message(MEDIA_URL, gui.playbackPanel.getMedia().getURL());
                output.writeObject(message);
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendMediaTime() {
            try {
                lastSentTime = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(MEDIA_TIME, gui.playbackPanel.getMedia().getCurrentTime()));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }

        private synchronized void sendMediaState() {
            try {
                lastSentState = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(MEDIA_STATE, gui.playbackPanel.getMedia().isPaused()));
                output.flush();
            } catch(IOException e) {
                disconnectFromServer();
            }
        }
    }
}
