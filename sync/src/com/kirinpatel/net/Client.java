package com.kirinpatel.net;

import com.kirinpatel.Main;
import com.kirinpatel.gui.ClientGUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.util.Debug;
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
    private ClientGUI gui;

    public Client(String ipAddress) {
        Debug.Log("Starting client...", 1);
        Client.ipAddress = ipAddress;
        Client.user = new User(System.getProperty("user.name"));

        clientThread = new ClientThread();
        new Thread(clientThread).start();
    }

    public static void stop() {
        Debug.Log("Stopping Client...", 1);
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

        public void run() {
            connectToServer();

            while(isConnected && isRunning) {
                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch(message.getType()) {
                            case 0:
                                if ((int) message.getMessage() == 3) {
                                    Debug.Log("Server closing...", 4);
                                    isServerClosed = true;
                                    gui.hide();
                                    Client.stop();
                                }
                                break;
                            case 11:
                                Debug.Log("Receiving list of connected clients...", 4);
                                ClientGUI.controlPanel.updateConnectedClients((ArrayList<User>) message.getMessage());
                                Debug.Log("Connected clients list received.", 4);
                                break;
                            case 20:
                                Debug.Log("Receiving media URL...", 4);
                                PlaybackPanel.mediaPlayer.setMediaURL(message.getMessage().toString());
                                Debug.Log("Media URL received.", 4);
                                break;
                            case 21:
                                Debug.Log("Received media state.", 4);
                                if ((boolean) message.getMessage()) PlaybackPanel.mediaPlayer.pause();
                                else PlaybackPanel.mediaPlayer.play();
                                break;
                            case 22:
                                Debug.Log("Receiving media time...", 4);
                                lastSentTime = (long) message.getMessage();
                                PlaybackPanel.mediaPlayer.seekTo(lastSentTime);
                                sendVideoState();
                                Debug.Log("Media time set.", 4);
                                break;
                            case 30:
                                Debug.Log("Receiving chat messages...", 4);
                                ClientGUI.controlPanel.addMessages((ArrayList<String>) message.getMessage());
                                Debug.Log("Chat messages received.", 4);
                                break;
                            default:
                                if (message.getMessage() != null) {
                                    Debug.Log("Unregistered message - (" + message.getType() + " : " + message.getMessage().toString() + ").", 1);
                                } else {
                                    Debug.Log("Unregistered message - (" + message.getType() + ").", 2);
                                }
                                break;
                        }
                    }
                } catch(IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (!messages.isEmpty()) {
                    sendMessages();
                }

                if (lastSentTime < (PlaybackPanel.mediaPlayer.getMediaTime() - 250) || lastSentTime > (PlaybackPanel.mediaPlayer.getMediaTime() + 50)) {
                    sendVideoTime();
                }
            }

            disconnectFromServer();
        }

        public void stop() {
            isConnected = false;
        }

        private synchronized void connectToServer() {
            try {
                Debug.Log("Connecting to server...", 4);
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
                Debug.Log("Connected to server.", 4);
            } catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            gui = new ClientGUI();
            Debug.Log("Client started.", 1);

            sendUsernameToServer();
        }

        private synchronized void disconnectFromServer() {
            try {
                Debug.Log("Disconnecting from server...", 4);
                if (output != null && !isServerClosed) {
                    output.writeObject(new Message(0, 0));
                    output.flush();
                } else if (isServerClosed) {
                    new UIMessage("Server shutdown.", "The sync server that you were connected to has shutdown.", 0);
                } else {
                    Debug.Log("Unable to send disconnect signal to server, forcefully disconnecting!", 5);
                }
            } catch(IOException e) {
                Debug.Log("Unable to send disconnect signal to server, forcefully disconnecting!", 5);
            }

            try {
                Debug.Log("Closing socket...", 4);
                if (socket != null && !socket.isClosed()) socket.close();
                Debug.Log("Socket closed.", 4);
            } catch(IOException e) {
                e.printStackTrace();
            }

            Debug.Log("Disconnected from server.", 4);
            Debug.Log("Client stopped.", 1);

            new Main();
        }

        private synchronized void sendUsernameToServer() {
            try {
                Debug.Log("Sending username to server...", 4);
                output.writeObject(new Message(10, user.getUsername()));
                output.flush();
                Debug.Log("Username sent to server.", 4);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendVideoTime() {
            try {
                Debug.Log("Sending current media time...", 4);
                output.writeObject(new Message(22, PlaybackPanel.mediaPlayer.getMediaTime()));
                output.flush();
                Debug.Log("Current media time sent.", 4);
                sendVideoState();
            } catch(SocketException e) {

            } catch(IOException e) {
                e.printStackTrace();
            }

            lastSentTime = PlaybackPanel.mediaPlayer.getMediaTime();
        }

        private synchronized void sendVideoState() {
            try {
                Debug.Log("Sending current media state...", 4);
                output.writeObject(new Message(21, PlaybackPanel.mediaPlayer.isPaused()));
                output.flush();
                Debug.Log("Current media state sent.", 4);
            } catch(SocketException e) {

            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void sendMessages() {
            try {
                Debug.Log("Sending chat messages to server...", 4);
                output.reset();
                output.writeObject(new Message(31, messages));
                output.flush();
                messages.clear();
                Debug.Log("Chat messages to server.", 4);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
