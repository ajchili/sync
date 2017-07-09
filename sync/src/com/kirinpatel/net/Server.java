package com.kirinpatel.net;

import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.tomcat.TomcatServer;
import com.kirinpatel.util.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static String ipAddress = "";
    private static GUI gui;
    private static ArrayList<String> messages = new ArrayList<>();
    private static ServerThread server;
    private static TomcatServer tomcatServer;
    private static boolean isRunning = false;
    private static boolean closeServer = false;
    private boolean isBound = false;

    public Server() {
        Debug.Log("Starting server...", 1);
        gui = new GUI(0);

        Main.connectedUsers.add(new User(System.getProperty("user.name") + " (host)"));
        GUI.controlPanel.updateConnectedClients(Main.connectedUsers);

        server = new ServerThread();
        new Thread(server).start();
        new Thread(() -> {
            tomcatServer = new TomcatServer();
            if (!Main.IS_MAC) tomcatServer.start();
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                if(isRunning) PortValidator.isAvailable(8000);
                if(isRunning && !Main.IS_MAC) PortValidator.isAvailable(8080);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void stop() {
        Debug.Log("Stopping server...", 1);
        if (Main.connectedUsers.size() == 1) {
            server.stop();
        } else {
            closeServer = true;
        }
    }

    public static void sendMessage(String message) {
        messages.add(message);
        GUI.controlPanel.setMessages(messages);
    }

    public static void kickUser(int user) {
        Main.connectedUsers.remove(user);
        GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
    }

    public static void setEnabled(boolean enabled) {
        gui.setEnabled(enabled);
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
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                connectionExecutor.shutdown();

                while(!connectionExecutor.isTerminated()) {

                }

                Debug.Log("Server stopped.", 1);

                if (isBound) {
                    new UIMessage("Unable to start server!", "The address is in use by another application!", 1);
                    Server.stop();
                }
            }
        }

        public void stop() {
            isRunning = false;

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }

            if (service != null && !service.isClosed()) {
                try {
                    service.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }

            if (!Main.IS_MAC) tomcatServer.stop();
        }

        /**
         * Provides the public IP address of the server.
         * Credit: https://stackoverflow.com/a/2939223
         *
         * @return Returns string value of public IP address
         */
        private String getIPAddress() {
            Debug.Log("Obtaining server IP address...", 4);
            String ip = "";
            try {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

                Debug.Log("Server IP address obtained.", 4);
                ipAddress = in.readLine();
                ip = " (" + ipAddress + ":8000)";
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                Debug.Log("Unable to obtain server IP address.", 5);
            }

            return ip;
        }
    }

    class ServerSocketTask implements Runnable {

        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User user;
        private String client = "client";
        private boolean isClientConnected = false;
        private boolean hasConnected = false;
        private String mediaURL = "";
        private boolean isPaused = false;
        private ArrayList<String> messages = new ArrayList<>();
        private long lastClientUpdate = System.currentTimeMillis() - 1000;
        private long time = 0;

        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            connectClientToServer();

            while(isClientConnected && isRunning) {
                if (closeServer) {
                    stop();

                    isClientConnected = false;
                    break;
                }

                if (hasConnected && !Main.connectedUsers.contains(user)) disconnectClientFromServer();

                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch(message.getType()) {
                            case 0:
                                if ((int) message.getMessage() == 0) {
                                    Main.connectedUsers.remove(user);
                                    GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                    isClientConnected = false;
                                    Debug.Log('C' + client.substring(1) + " disconnected.".substring(1), 4);
                                }
                                break;
                            case 10:
                                user = new User(message.getMessage().toString());
                                Main.connectedUsers.add(user);
                                client += " (" + user.getUsername() + ':' + user.getUserID() + ')';
                                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                hasConnected = true;
                                break;
                            case 21:
                                if ((boolean) message.getMessage() != isPaused) sendVideoState();
                                break;
                            case 22:
                                time = (long) message.getMessage();
                                if (user != null) user.setTime(time);
                                sendVideoRate();
                                break;
                            case 31:
                                for (String m : (ArrayList<String>) message.getMessage()) {
                                    sendMessage(m);
                                }
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
                    disconnectClientFromServer();
                }

                if (System.currentTimeMillis() > lastClientUpdate + 1250) sendConnectedUsersToClient();

                if (messages.size() < Server.messages.size()) sendMessagesToClient();

                if (!mediaURL.equals(PlaybackPanel.mediaPlayer.getMediaURL())) sendMediaURL();

                if (isPaused != PlaybackPanel.mediaPlayer.isPaused()) sendVideoState();
            }

            try {
                Debug.Log("Closing socket...", 4);
                socket.close();
                Debug.Log("Socket closed.", 4);
            } catch(IOException e) {
                Main.connectedUsers.remove(user);
                stop();
            }
        }

        public void stop() {
            disconnectClientFromServer();

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
                Debug.Log("Established connection to " + client + '.', 4);
                sendVideoState();
                sendVideoTime();
                sendConnectedUsersToClient();
            } catch(IOException | ClassNotFoundException e) {
                Debug.Log("Unable to establish connection to " + client + '.', 5);
            }
        }

        private synchronized void disconnectClientFromServer() {
            try {
                Debug.Log("Sending disconnect message to " + client + "...", 4);
                output.writeObject(new Message(0, 3));
                output.flush();
                Debug.Log("Disconnect message sent.", 4);
            } catch(IOException e) {
                Debug.Log("Unable to properly disconnect with client.", 5);
                isClientConnected = false;
            }
        }

        private synchronized void sendConnectedUsersToClient() {
            try {
                lastClientUpdate = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(11, Main.connectedUsers));
                output.flush();
            } catch(IOException e) {
                Debug.Log("Unable to send connected clients list to " + client + '.', 5);
                disconnectClientFromServer();
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
                output.flush();
                output.writeObject(new Message(30, newMessages));
                output.flush();
            } catch(IOException e) {
                Debug.Log("Unable to send message log to " + client + '.', 5);
                disconnectClientFromServer();
            }
        }

        private synchronized void sendMediaURL() {
            try {
                output.writeObject(new Message(20, PlaybackPanel.mediaPlayer.getMediaURL()));
                output.flush();
                mediaURL = PlaybackPanel.mediaPlayer.getMediaURL();
                time = 0;
                Main.connectedUsers.get(0).setTime(PlaybackPanel.mediaPlayer.getMediaTime());
            } catch(IOException e) {
                Debug.Log("Unable to send media URL to " + client + '.', 5);
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoState() {
            try {
                output.writeObject(new Message(21, PlaybackPanel.mediaPlayer.isPaused()));
                output.flush();
                isPaused = PlaybackPanel.mediaPlayer.isPaused();
            } catch(IOException e) {
                Debug.Log("Unable to send media state to " + client + '.', 5);
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoTime() {
            try {
                output.reset();
                output.writeObject(new Message(22, PlaybackPanel.mediaPlayer.getMediaTime()));
                output.flush();
            } catch(IOException e) {
                Debug.Log("Unable to send current media time to " + client + '.', 5);
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoRate() {
            long timeDifference = PlaybackPanel.mediaPlayer.getMediaTime() - time;
            if (Math.abs(timeDifference) > 2000) {
                Debug.Log('C' + client.substring(1) + " is out of sync by " + timeDifference + " milliseconds, sending current media time.", 4);
                sendVideoTime();
            } else if (Math.abs(timeDifference) > 1000) {
                try {
                    Debug.Log('C' + client.substring(1) + " is out of sync by " + timeDifference + " milliseconds, changing playback rate.", 4);
                    output.writeObject(new Message(23, (timeDifference + 1000) * 1.0f / 1000));
                    output.flush();
                } catch(IOException e) {
                    Debug.Log("Unable to send rate to " + client + '.', 5);
                    disconnectClientFromServer();
                }
            }
        }
    }
}
