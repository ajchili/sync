package com.kirinpatel.net;

import com.kirinpatel.Main;
import com.kirinpatel.gui.ClientGUI;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.Message;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Kirin Patel
 * @version 0.0.2
 * @date 6/16/17
 */
public class Client {

    public static String ipAddress;
    public static User user;
    private Socket socket;
    private ClientGUI gui;
    private static ClientThread clientThread;
    private static boolean isRunning = false;
    private static boolean isServerClosed = false;

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

        public void run() {
            connectToServer();

            while (isConnected && isRunning) {
                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch (message.getType()) {
                            case 0:
                                if ((int) message.getMessage() == 3) {
                                    Debug.Log("Server closing...", 4);
                                    isServerClosed = true;
                                    gui.hide();
                                    Client.stop();
                                }
                                break;
                            default:
                                Debug.Log("Unregistered message - (" + message.getType() + " : " + message.getMessage().toString() + ").", 1);
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
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
            } catch (UnknownHostException e) {
                new UIMessage("Error joining server!", "The IP Address provided is not running!", 1);
                Client.stop();
                return;
            } catch (ConnectException e) {
                new UIMessage("Error joining server!", "Server is either full or not running!", 1);
                Client.stop();
                return;
            } catch (IOException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
                } if (isServerClosed) {
                    new Main();
                    new UIMessage("Server shutdown.", "The sync server that you were connected to has shutdown.", 0);
                    return;
                } else {
                    Debug.Log("Unable to send disconnect signal to server, forcefully disconnecting!", 5);
                    new Main();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Debug.Log("Closing socket...", 4);
                socket.close();
                Debug.Log("Socket closed.", 4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Debug.Log("Disconnected from server.", 4);
            Debug.Log("Client stopped.", 1);
        }

        private synchronized void sendUsernameToServer() {
            try {
                Debug.Log("Sending username to server...", 4);
                output.writeObject(new Message(10, user.getUsername()));
                output.flush();
                Debug.Log("Username sent to server.", 4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
