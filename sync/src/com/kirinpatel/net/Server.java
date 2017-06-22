package com.kirinpatel.net;

import com.kirinpatel.gui.ServerGUI;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.Message;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.User;
import javafx.util.Duration;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kirin Patel
 * @version 0.0.7
 * @date 6/16/17
 */
public class Server {

    private static ServerGUI gui;
    public static ArrayList<User> connectedClients = new ArrayList<>();
    private static ArrayList<String> messages = new ArrayList<>();
    private static ServerThread server;
    private static boolean isRunning = false;
    private static boolean closeServer = false;
    private static boolean isBound = false;

    public Server() {
        Debug.Log("Starting server...", 1);
        gui = new ServerGUI();

        connectedClients.add(new User(System.getProperty("user.name") + " (host)"));
        ServerGUI.serverControlPanel.updateConnectedClients(connectedClients);

        server = new ServerThread();
        new Thread(server).start();
    }

    public static void stop() {
        Debug.Log("Stopping server...", 1);
        if (connectedClients.size() == 1) {
            server.stop();
        } else {
            closeServer = true;
        }
    }

    public static void sendMessage(String message) {
        messages.add(message);
        ServerGUI.serverControlPanel.setMessages(messages);
    }

    class ServerThread implements Runnable {

        private ExecutorService connectionExecutor;
        private ServerSocket service;
        private Socket socket;

        public void run() {
            isRunning = true;

            connectionExecutor = Executors.newFixedThreadPool(10);

            try {
                service = new ServerSocket(8000);

                Debug.Log("Server started.", 1);
                gui.setTitle(gui.getTitle() + getIPAddress());
                while(isRunning) {
                    Debug.Log("Awaiting connection...", 4);
                    socket = service.accept();

                    connectionExecutor.execute(new ServerSocketTask(socket));
                }
            } catch(BindException e) {
                Debug.Log("Unable to start server, address already in use!", 5);
                gui.dispose();
                isBound = true;
            } catch(SocketException e) {
                Debug.Log("Closing unused socket...", 4);
                Debug.Log("Socket closed.", 4);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connectionExecutor.shutdown();

                while (!connectionExecutor.isTerminated()) {

                }

                Debug.Log("Server stopped.", 1);

                if (isBound) {
                    new UIMessage("Unable to start server!", "The address is in use by another application!", 1);
                }
            }
        }

        public void stop() {
            isRunning = false;

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (service != null &&!service.isClosed()) {
                try {
                    service.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Provides the public IP address of the server.
         * Credit: https://stackoverflow.com/a/2939223
         *
         * @return Returns string value of public IP address
         */
        private String getIPAddress() {
            Debug.Log("Obtaining server IP address...", 4);
            try {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

                Debug.Log("Server IP address obtained.", 4);
                return " (" +  in.readLine() + ":8000)";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Debug.Log("Unable to obtain server IP address.", 5);
            return "";
        }
    }

    class ServerSocketTask implements Runnable {

        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User user;
        private String client = "client";
        private boolean isClientConnected = false;
        private String mediaURL = "";
        private boolean isPaused = false;
        private ArrayList<String> messages = new ArrayList<>();
        private long lastClientUpdate = System.currentTimeMillis() - 9000;
        private Duration time = new Duration(0);

        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            connectClientToServer();

            while (isClientConnected && isRunning) {
                if (closeServer) {
                    stop();

                    isClientConnected = false;
                    break;
                }

                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch (message.getType()) {
                            case 0:
                                if ((int) message.getMessage() == 0) {
                                    Debug.Log("Disconnecting " + client + "...", 4);
                                    connectedClients.remove(user);
                                    ServerGUI.serverControlPanel.updateConnectedClients(connectedClients);
                                    isClientConnected = false;
                                    Debug.Log("C" + client + " disconnected.".substring(1), 4);
                                }
                                break;
                            case 10:
                                Debug.Log("Receiving " + client + " username...", 4);
                                user = new User(message.getMessage().toString());
                                Debug.Log("Received " + client + " username.", 4);
                                connectedClients.add(user);
                                client += " (" + user.getUsername() + ":" + user.getUserID() + ")";
                                ServerGUI.serverControlPanel.updateConnectedClients(connectedClients);
                                break;
                            case 24:
                                Debug.Log("Receiving " + client + " time...",4);
                                time = (Duration) message.getMessage();
                                break;
                            case 31:
                                Debug.Log("Receiving " + client + " messages...", 4);
                                for (String m : (ArrayList<String>) message.getMessage()) {
                                    sendMessage(m);
                                }
                                Debug.Log("Client " + client + " received.", 4);
                                break;
                            default:
                                if (message.getType() == 24) {
                                    break;
                                }

                                if (message.getMessage() != null) {
                                    Debug.Log("Unregistered message - (" + message.getType() + " : " + message.getMessage().toString() + ").", 1);
                                } else {
                                    Debug.Log("Unregistered message - (" + message.getType() + ").", 2);
                                }
                                break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (System.currentTimeMillis() > lastClientUpdate + 10000) {
                    sendConnectedUsersToClient();
                }

                if (messages.size() < Server.messages.size()) {
                    sendMessagesToClient();
                }

                if (!mediaURL.equals(ServerGUI.mediaPanel.getMediaURL())) {
                    sendMediaURL();
                }

                if (isPaused != ServerGUI.mediaPanel.isMediaPaused()) {
                    sendVideoState(ServerGUI.mediaPanel.isMediaPaused());
                }

                if (time.toMillis() < (ServerGUI.mediaPanel.getMediaTime().toMillis() - 2000) || time.toMillis() > (ServerGUI.mediaPanel.getMediaTime().toMillis() + 1000)) {
                    sendVideoTime();
                }
            }

            try {
                Debug.Log("Closing socket...", 4);
                socket.close();
                Debug.Log("Socket closed.", 4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            try {
                Debug.Log("Sending closing message to " + client + "...",4);
                output.writeObject(new Message(0, 3));
                output.flush();
                Debug.Log("Closing message sent.",4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            server.stop();
        }

        private synchronized void connectClientToServer() {
            try {
                Debug.Log("Establishing connection to " + client + "...", 4);
                input = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) input.readObject();
                isClientConnected = message.getType() == 0 && (int) message.getMessage() == 1;
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message(0, 2));
                output.flush();
                Debug.Log("Established connection to " + client + ".", 4);
            } catch (IOException | ClassNotFoundException e) {
                Debug.Log("Unable to establish connection to " + client + ".", 5);
            }
        }

        private synchronized void sendConnectedUsersToClient() {
            try {
                lastClientUpdate = System.currentTimeMillis();
                Debug.Log("Sending connected clients list to " + client + "...", 4);
                output.reset();
                output.writeObject(new Message(11, connectedClients));
                output.flush();
                Debug.Log("Connected clients list sent to " + client + ".", 4);
            } catch (IOException e) {
                Debug.Log("Unable to send connected clients list to " + client + ".", 5);
            }
        }

        private synchronized void sendMessagesToClient() {
            try {
                ArrayList<String> newMessages = new ArrayList<>();
                ArrayList<String> messageCache = Server.messages;
                for (int i = messages.size(); i < messageCache.size(); i++) {
                    newMessages.add(messageCache.get(i));
                }
                messages.clear();
                messages.addAll(messageCache);
                Debug.Log("Sending message log to " + client + "...", 4);
                output.flush();
                output.writeObject(new Message(30, newMessages));
                output.flush();
                Debug.Log("Message log sent to " + client + ".", 4);
            } catch (IOException e) {
                Debug.Log("Unable to send message log to " + client + ".", 5);
            }
        }

        private synchronized void sendMediaURL() {
            try {
                Debug.Log("Sending media URL to " + client + "...", 4);
                output.writeObject(new Message(20, ServerGUI.mediaPanel.getMediaURL()));
                output.flush();
                Debug.Log("Media URL sent to " + client + ".", 4);
                mediaURL = ServerGUI.mediaPanel.getMediaURL();
            } catch (IOException e) {
                Debug.Log("Unable to send media URL to " + client + ".", 5);
            }
        }

        private synchronized void sendVideoState(boolean isPaused) {
            Debug.Log("Sending media state to " + client + "...", 4);
            if (isPaused) {
                try {
                    output.writeObject(new Message(22, null));
                    output.flush();
                } catch (IOException e) {
                    Debug.Log("Unable to send media state to " + client + ".", 5);
                }
            } else {
                try {
                    output.writeObject(new Message(21, null));
                    output.flush();
                } catch (IOException e) {
                    Debug.Log("Unable to send media state to " + client + ".", 5);
                }
            }
            Debug.Log("Media state sent to " + client + ".", 4);
            this.isPaused = ServerGUI.mediaPanel.isMediaPaused();
            sendVideoTime();
        }

        private synchronized void sendVideoTime() {
            try {
                Debug.Log("Sending current media time to " + client + "...", 4);
                output.writeObject(new Message(23, time.add(new Duration(ServerGUI.mediaPanel.getMediaTime().toMillis() - time.toMillis()))));
                output.flush();
                Debug.Log("Current media time sent to " + client + ".", 4);
            } catch (IOException e) {
                Debug.Log("Unable to send current media time to " + client + ".", 5);
            }

            time = ServerGUI.mediaPanel.getMediaTime();
        }
    }
}
