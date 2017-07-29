package com.kirinpatel.net;

import static com.kirinpatel.util.Message.MESSAGE_TYPE.*;

import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.util.Media;
import com.kirinpatel.util.Message;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;

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
    private static boolean isRunning = false;
    private static boolean isServerClosed = false;
    private Socket socket;
    private static GUI gui;

    public Client(String ipAddress) {
        Client.ipAddress = ipAddress;
        Client.user = new User(System.getProperty("user.name"));

        clientThread = new ClientThread();
        new Thread(clientThread).start();
    }

    public static void stop() {
        if (gui.isVisible()) {
            gui.hide();
        }
        clientThread.stop();

        new Main();
    }

    public static void sendMessage(String message) {
        messages.add(message);
    }

    class ClientThread implements Runnable {

        private ObjectInputStream input;
        private ObjectOutputStream output;
        private boolean isConnected = false;
        private long lastSentTime = 0;

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
                                Main.connectedUsers = (ArrayList<User>) message.getMessage();
                                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                break;
                            case MEDIA:
                                // TODO: make this readable/conform to style standards
                                Media media = (Media) message.getMessage();
                                media.setCurrentTime(media.getCurrentTime() + user.getPing());
                                long timeDifference = PlaybackPanel.mediaPlayer.getMedia().getCurrentTime() - media.getCurrentTime();
                                if (!PlaybackPanel.mediaPlayer.getMedia().getURL().equals(media.getURL())) {
                                    lastSentTime = 0;
                                    user.setMedia(media);
                                    PlaybackPanel.mediaPlayer.setMedia(media);
                                    if (!media.isPaused()) {
                                        PlaybackPanel.mediaPlayer.play();
                                    }
                                } else {
                                    if (media.isPaused()) {
                                        PlaybackPanel.mediaPlayer.pause();
                                    } else {
                                        PlaybackPanel.mediaPlayer.play();
                                    }
                                    if (media.getRate() != 1.0f) {
                                        PlaybackPanel.mediaPlayer.setRate(media.getRate());
                                    }
                                    if (Math.abs(timeDifference) > 2000) {
                                        PlaybackPanel.mediaPlayer.seekTo(media.getCurrentTime());
                                    } else if (Math.abs(timeDifference) > 1000) {
                                        PlaybackPanel.mediaPlayer.setRate((timeDifference + 1000) * 1.0f / 1000);
                                    }
                                }
                                break;
                            case MESSAGES:
                                GUI.controlPanel.addMessages((ArrayList<String>) message.getMessage());
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

                if (PlaybackPanel.mediaPlayer.getMedia().isPaused()) {
                    if (lastSentTime < (System.currentTimeMillis() - 500)) {
                        sendMedia();
                    }
                } else {
                    if (lastSentTime < (PlaybackPanel.mediaPlayer.getMedia().getCurrentTime() - 500)) {
                        sendMedia();
                    }
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

            gui = new GUI(1);
            Main.saveIPAddress(ipAddress);

            sendUsernameToServer();
        }

        private synchronized void disconnectFromServer() {
            boolean couldNotDisconnect = false;
            try {
                if (output != null && !isServerClosed) {
                    isConnected = false;
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
                couldNotDisconnect = true;
                Client.stop();
            } finally {
                if (!couldNotDisconnect) {
                    new Main();
                }
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

        private synchronized void sendMedia() {
            try {
                lastSentTime = PlaybackPanel.mediaPlayer.getMedia().getCurrentTime();
                user.setMedia(PlaybackPanel.mediaPlayer.getMedia());
                output.reset();
                output.writeObject(new Message(MEDIA, user.getMedia()));
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
    }
}
