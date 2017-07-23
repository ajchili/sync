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
        gui = new GUI(0);

        Main.connectedUsers.add(new User(System.getProperty("user.name") + " (host)"));
        GUI.controlPanel.updateConnectedClients(Main.connectedUsers);

        server = new ServerThread();
        new Thread(server).start();
        new Thread(() -> {
            tomcatServer = new TomcatServer();
            tomcatServer.start();
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                if(isRunning) {
                    PortValidator.isAvailable(8000);
                    PortValidator.isAvailable(8080);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void stop() {
        if (Main.connectedUsers.size() == 1) server.stop();
        else closeServer = true;
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

                gui.setTitle(gui.getTitle() + getIPAddress());
                while(isRunning) {
                    socket = service.accept();

                    connectionExecutor.execute(new ServerSocketTask(socket));
                }
            } catch(BindException e) {
                gui.dispose();
                isBound = true;
            } catch(SocketException e) {
                System.out.println("Catch this exception better, smh. What is wrong with you????");
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                connectionExecutor.shutdown();

                while(!connectionExecutor.isTerminated()) {

                }

                if (isBound) {
                    new UIMessage("Unable to start server!",
                            "The address is in use by another application!",
                            1);
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

            tomcatServer.stop();
        }

        /**
         * Provides the public IP address of the server.
         * Credit: https://stackoverflow.com/a/2939223
         *
         * @return Returns string value of public IP address
         */
        private String getIPAddress() {
            String ip = "";
            try {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

                ipAddress = in.readLine();
                ip = " (" + ipAddress + ":8000)";
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                System.out.println("Catch this exception better, smh. What is wrong with you????");
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
        private long lastPingCheck = System.currentTimeMillis() - 1000;
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
                                switch((int) message.getMessage()) {
                                    case 0:
                                        Main.connectedUsers.remove(user);
                                        GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                        isClientConnected = false;
                                        break;
                                    case 4:
                                        user.setPing(System.currentTimeMillis() - lastPingCheck);
                                        break;
                                    default:
                                        break;
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
                                System.out.println("Catch this exception better, smh. What is wrong with you????");
                                break;
                        }
                    }
                } catch(IOException | ClassNotFoundException e) {
                    disconnectClientFromServer();
                }

                if (System.currentTimeMillis() > lastPingCheck + 1000) sendPing();

                if (System.currentTimeMillis() > lastClientUpdate + 1000) sendConnectedUsersToClient();

                if (messages.size() < Server.messages.size()) sendMessagesToClient();

                if (!mediaURL.equals(PlaybackPanel.mediaPlayer.getMediaURL())) sendMediaURL();

                if (isPaused != PlaybackPanel.mediaPlayer.isPaused()) sendVideoState();
            }

            try {
                socket.close();
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
                input = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) input.readObject();
                isClientConnected = message.getType() == 0 && (int) message.getMessage() == 1;
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message(0, 2));
                output.flush();
                sendVideoState();
                sendVideoTime();
                sendConnectedUsersToClient();
            } catch(IOException | ClassNotFoundException e) {
                System.out.println("Catch this exception better, smh. What is wrong with you????");
            }
        }

        private synchronized void disconnectClientFromServer() {
            try {
                output.writeObject(new Message(0, 3));
                output.flush();
            } catch(IOException e) {
                System.out.println("Catch this exception better, smh. What is wrong with you????");
            } finally {
                isClientConnected = false;
            }
        }

        private synchronized void sendPing() {
            try {
                lastPingCheck = System.currentTimeMillis();
                output.writeObject(new Message(0, 4));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendConnectedUsersToClient() {
            try {
                lastClientUpdate = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(11, Main.connectedUsers));
                output.flush();
            } catch(IOException e) {
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
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoState() {
            try {
                output.writeObject(new Message(21, PlaybackPanel.mediaPlayer.isPaused()));
                output.flush();
                isPaused = PlaybackPanel.mediaPlayer.isPaused();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoTime() {
            try {
                output.reset();
                output.writeObject(new Message(22, PlaybackPanel.mediaPlayer.getMediaTime()));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoRate() {
            long timeDifference = PlaybackPanel.mediaPlayer.getMediaTime() - time;
            if (Math.abs(timeDifference) > 2000) {
                sendVideoTime();
            } else if (Math.abs(timeDifference) > 1000) {
                try {
                    output.writeObject(new Message(23, (timeDifference + 1000) * 1.0f / 1000));
                    output.flush();
                } catch(IOException e) {
                    disconnectClientFromServer();
                }
            }
        }
    }
}
