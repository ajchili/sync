package com.kirinpatel.net;

import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.util.Message;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {

    public static String ipAddress;
    public static User user;
    private static ClientThread clientThread;
    private static ArrayList<String> messages = new ArrayList<>();
    private static boolean isRunning = false;
    private static boolean isServerClosed = false;
    private Socket socket;
    private GUI gui;

    public Client(String ipAddress) {
        Client.ipAddress = ipAddress;
        Client.user = new User(System.getProperty("user.name"));

        clientThread = new ClientThread();
        new Thread(clientThread).start();
    }

    public static void stop() {
        clientThread.stop();
    }

    public static void sendMessage(String message) {
        messages.add(message);
    }

    class ClientThread implements Runnable {

        private ClientCommunicationThread clientCommunicationThread;

        public void run() {
            isRunning = true;

            clientCommunicationThread = new ClientCommunicationThread();
            new Thread(clientCommunicationThread).start();
        }

        public void stop() {
            isRunning = false;

            clientCommunicationThread.stop();
        }
    }

    class ClientCommunicationThread implements Runnable {

        private ObjectInputStream input;
        private ObjectOutputStream output;
        private boolean isConnected = false;
        private long lastSentTime = 0;
        private float rate = 1.0f;

        public void run() {
            connectToServer();

            while(isConnected && isRunning) {
                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch(message.getType()) {
                            case 0:
                                switch((int) message.getMessage()) {
                                    case 3:
                                        isServerClosed = true;
                                        gui.hide();
                                        Client.stop();
                                        stop();
                                        break;
                                    case 4:
                                        sendPing();
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case 11:
                                Main.connectedUsers = (ArrayList<User>) message.getMessage();
                                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                break;
                            case 20:
                                PlaybackPanel.mediaPlayer.setMediaURL(message.getMessage().toString());
                                lastSentTime = 0;
                                break;
                            case 21:
                                if ((boolean) message.getMessage()) PlaybackPanel.mediaPlayer.pause();
                                else PlaybackPanel.mediaPlayer.play();
                                break;
                            case 22:
                                PlaybackPanel.mediaPlayer.seekTo((long) message.getMessage());
                                lastSentTime = PlaybackPanel.mediaPlayer.getMediaTime();
                                sendVideoState();
                                break;
                            case 23:
                                rate = ((float) message.getMessage() > 0.75f ? (float) message.getMessage() : 0.75f);
                                PlaybackPanel.mediaPlayer.setRate(rate);
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(200);
                                        PlaybackPanel.mediaPlayer.setRate(1.0f);
                                    } catch(InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                            case 30:
                                GUI.controlPanel.addMessages((ArrayList<String>) message.getMessage());
                                break;
                            default:
                                System.out.println("Catch this exception better, smh. What is wrong with you????");
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
            isConnected = false;
        }

        private synchronized void connectToServer() {
            try {
                socket = new Socket(Client.ipAddress, 8000);
                socket.setKeepAlive(true);
            } catch(UnknownHostException e) {
                new UIMessage("Error joining server!", "The IP Address provided is not running!", 1);
                Client.stop();
                return;
            } catch(ConnectException e) {
                new UIMessage("Error joining server!", "Server is either full or not running!", 1);
                Client.stop();
                return;
            } catch(SocketException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message(0, 1));
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) input.readObject();
                isConnected = message.getType() == 0 && (int) message.getMessage() == 2;
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
                    output.writeObject(new Message(0, 0));
                    output.flush();
                } else if (isServerClosed) {
                    new UIMessage("Server shutdown.", "The sync server that you were connected to has shutdown.", 0);
                }
            } catch(IOException e) {
                System.out.println("Catch this exception better, smh. What is wrong with you????");
            }

            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            new Main();
        }

        private synchronized void sendPing() {
            try {
                output.writeObject(new Message(0, 4));
                output.flush();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendUsernameToServer() {
            try {
                output.writeObject(new Message(10, user.getUsername()));
                output.flush();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendVideoState() {
            try {
                output.writeObject(new Message(21, PlaybackPanel.mediaPlayer.isPaused()));
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
                output.writeObject(new Message(22, lastSentTime));
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
                output.writeObject(new Message(31, messages));
                output.flush();
                messages.clear();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
