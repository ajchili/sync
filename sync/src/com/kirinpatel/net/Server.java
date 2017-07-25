package com.kirinpatel.net;

import static com.kirinpatel.util.Message.MESSAGE_TYPE.*;
import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.tomcat.TomcatServer;
import com.kirinpatel.util.*;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static String ipAddress = "";
    private static GUI gui;
    private static GatewayDiscover discover;
    private static GatewayDevice device;
    private static ServerThread server;
    private static TomcatServer tomcatServer;
    private static ArrayList<String> messages = new ArrayList<>();
    private static boolean isRunning = false;
    private static boolean closeServer = false;
    private boolean isBound = false;

    public Server() {
        gui = new GUI(0);

        Main.connectedUsers.add(new User(System.getProperty("user.name") + " (host)"));
        GUI.controlPanel.updateConnectedClients(Main.connectedUsers);

        server = new ServerThread();
        new Thread(server).start();
    }

    public static void stop() {
        if (Main.connectedUsers.size() == 1) {
            server.stop();
        }
        else {
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

    /**
     * Sets whether the server GUI will be usable or not.
     *
     * @param enabled Is usable
     */
    public static void setEnabled(boolean enabled) {
        gui.setEnabled(enabled);
    }

    class ServerThread implements Runnable {

        private ExecutorService connectionExecutor;
        private ServerSocket service;
        private Socket socket;

        public void run() {
            isRunning = true;

            discover = new GatewayDiscover();
            try {
                discover.discover();
                device = discover.getValidGateway();

                if (device != null) {
                    if (!device.addPortMapping(8000, 8000, device.getLocalAddress().getHostAddress(),"TCP","sync")) {
                        new UIMessage("UPnP not enabled!"
                                ,"Please enable UPnP in your router's settings to allow clients to connect" +
                                "to your sync server."
                                ,1);
                    }

                    if (!device.addPortMapping(8080, 8080, device.getLocalAddress().getHostAddress(),"TCP","tomcat")) {
                        new UIMessage("UPnP not enabled!"
                                ,"Please enable UPnP in your router's settings to allow clients to connect" +
                                "to your sync server."
                                ,1);
                    } else {
                        tomcatServer = new TomcatServer();
                        new Thread(() -> tomcatServer.start()).start();
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            } catch(SAXException e) {
                e.printStackTrace();
            } catch(ParserConfigurationException e) {
                e.printStackTrace();
            }

            connectionExecutor = Executors.newFixedThreadPool(10);

            try {
                service = new ServerSocket(8000);

                try {
                    gui.setTitle(gui.getTitle() + " (" + device.getExternalIPAddress() + ":8000)");
                    gui.setVisible(true);
                } catch(SAXException e) {
                    server.stop();
                    new UIMessage("Unable to start server!"
                            ,"Please ensure that UPnP is enabled in your router."
                            , 1);
                }

                while (isRunning) {
                    socket = service.accept();

                    connectionExecutor.execute(new ServerSocketTask(socket));
                }
            } catch(BindException e) {
                gui.dispose();
                isBound = true;
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                connectionExecutor.shutdown();
                while(!connectionExecutor.isTerminated()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
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

            if (device != null) {
                try {
                    device.deletePortMapping(8000,"TCP");
                    device.deletePortMapping(8080,"TCP");
                } catch(IOException e) {
                    e.printStackTrace();
                } catch(SAXException e) {
                    e.printStackTrace();
                }
            }

            try {
                if (socket != null) {
                    socket.close();
                }
                if (service != null) {
                    service.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (tomcatServer != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        tomcatServer.stop();
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    class ServerSocketTask implements Runnable {

        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User user;
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
                            case DISCONNECTING:
                                Main.connectedUsers.remove(user);
                                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                isClientConnected = false;
                                break;
                            case PING_TESTING:
                                user.setPing(System.currentTimeMillis() - lastPingCheck);
                                break;
                            case CLIENT_NAME:
                                user = new User(message.getMessage().toString());
                                Main.connectedUsers.add(user);
                                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
                                hasConnected = true;
                                break;
                            case MEDIA_STATE:
                                if ((boolean) message.getMessage() != isPaused) sendVideoState();
                                break;
                            case TIME:
                                time = (long) message.getMessage();
                                if (user != null) user.setTime(time);
                                sendVideoRate();
                                break;
                            case CLIENT_MESSAGES:
                                for (String m : (ArrayList<String>) message.getMessage()) {
                                    sendMessage(m);
                                }
                                break;
                            default:
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
                isClientConnected = message.getType() == CONNECTING;
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message(CONNECTED, ""));
                output.flush();
                sendVideoState();
                sendVideoTime();
                sendConnectedUsersToClient();
            } catch(IOException | ClassNotFoundException e) {
                // TODO(ajchili): catch this better
            }
        }

        private synchronized void disconnectClientFromServer() {
            try {
                output.writeObject(new Message(CLOSING, ""));
                output.flush();
            } catch(IOException e) {
                // TODO(ajchili): catch this better
            } finally {
                isClientConnected = false;
            }
        }

        private synchronized void sendPing() {
            try {
                lastPingCheck = System.currentTimeMillis();
                output.writeObject(new Message(PING_TESTING, ""));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendConnectedUsersToClient() {
            try {
                lastClientUpdate = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(CONNECTED_CLIENTS, Main.connectedUsers));
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
                output.writeObject(new Message(MESSAGES, newMessages));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendMediaURL() {
            try {
                output.writeObject(new Message(MEDIA_URL, PlaybackPanel.mediaPlayer.getMediaURL()));
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
                output.writeObject(new Message(MEDIA_STATE, PlaybackPanel.mediaPlayer.isPaused()));
                output.flush();
                isPaused = PlaybackPanel.mediaPlayer.isPaused();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendVideoTime() {
            try {
                output.reset();
                output.writeObject(new Message(TIME, PlaybackPanel.mediaPlayer.getMediaTime()));
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
                    output.writeObject(new Message(PLAYBACK_RATE, (timeDifference + 1000) * 1.0f / 1000));
                    output.flush();
                } catch(IOException e) {
                    disconnectClientFromServer();
                }
            }
        }
    }
}
