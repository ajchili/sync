package com.kirinpatel.net;

import static com.kirinpatel.util.Message.MESSAGE_TYPE.*;

import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.util.Message;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
        if (gui.isVisible()){
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
        private float rate = 1.0f;

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
                            case PING_TESTING:
                                sendPing();
                                break;
                            case CONNECTED_CLIENTS:
                                Main.connectedUsers = (ArrayList<User>) message.getMessage();
                                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                break;
                            case MEDIA_URL:
                                PlaybackPanel.mediaPlayer.setMediaURL(message.getMessage().toString());
                                lastSentTime = 0;
                                break;
                            case MEDIA_STATE:
                                if ((boolean) message.getMessage()) PlaybackPanel.mediaPlayer.pause();
                                else PlaybackPanel.mediaPlayer.play();
                                break;
                            case TIME:
                                PlaybackPanel.mediaPlayer.seekTo((long) message.getMessage());
                                lastSentTime = PlaybackPanel.mediaPlayer.getMediaTime();
                                sendVideoState();
                                break;
                            case PLAYBACK_RATE:
                                rate = ((float) message.getMessage() > 0.75f ? (float) message.getMessage() : 0.75f);
                                PlaybackPanel.mediaPlayer.setRate(rate);
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(200);
                                        PlaybackPanel.mediaPlayer.setRate(1.0f);
                                    } catch(InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        e.printStackTrace();
                                    }
                                }).start();
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

                if (!messages.isEmpty()) sendMessages();

                if (lastSentTime < (PlaybackPanel.mediaPlayer.getMediaTime() - 250)) sendVideoTime();
            }

            disconnectFromServer();
        }

        public void stop() {
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
                e.printStackTrace();
            }

            gui = new GUI(1);
            Main.saveIPAddress(ipAddress);

            sendUsernameToServer();
        }

        private synchronized void disconnectFromServer() {
            try {
                if (output != null && !isServerClosed) {
                    output.writeObject(new Message(DISCONNECTING, ""));
                    output.flush();
                } else if (isServerClosed) {
                    UIMessage.showMessageDialog(
                            "The sync server that you were connected to has shutdown.",
                            "Server shut down.");
                }
            } catch(IOException e) {
                // TODO(ajchili): catch this better
            }

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }

            new Main();
        }

        private synchronized void sendPing() {
            try {
                output.writeObject(new Message(PING_TESTING, ""));
                output.flush();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendUsernameToServer() {
            try {
                output.writeObject(new Message(CLIENT_NAME, user.getUsername()));
                output.flush();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendVideoState() {
            try {
                output.writeObject(new Message(MEDIA_STATE, PlaybackPanel.mediaPlayer.isPaused()));
                output.flush();
            } catch(SocketException e) {
                Client.stop();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendVideoTime() {
            try {
                lastSentTime = PlaybackPanel.mediaPlayer.getMediaTime();
                output.reset();
                output.writeObject(new Message(TIME, lastSentTime));
                output.flush();
                sendVideoState();
            } catch(SocketException e) {
                Client.stop();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendMessages() {
            try {
                output.reset();
                output.writeObject(new Message(CLIENT_MESSAGES, messages));
                output.flush();
                messages.clear();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
