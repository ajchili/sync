package com.kirinpatel.sync.net;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

import com.kirinpatel.sync.Launcher;
import com.kirinpatel.sync.gui.ControlPanel;
import com.kirinpatel.sync.gui.GUI;
import com.kirinpatel.sync.gui.MediaSelectorGUI;
import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.utils.Message;
import com.kirinpatel.sync.utils.UIMessage;
import com.kirinpatel.sync.utils.User;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static String ipAddress = "";
    public static GUI gui;
    private static ServerThread server;
    private static TomcatServer tomcatServer;
    private static final ArrayList<String> messages = new ArrayList<>();
    private static boolean isRunning = false;
    private static boolean closeServer = false;

    private final static int SYNC_PORT = 8000;
    final static int TOMCAT_PORT = 8080;

    public Server() {
        gui = new GUI(SERVER);
        Sync.connectedUsers.add(new User(System.getProperty("user.name") + " (host)"));
        Sync.host = Sync.connectedUsers.get(0);
        ControlPanel.getInstance().updateConnectedClients();
        server = new ServerThread();
        new Thread(server).start();
    }

    public static void stop() {
        GUI.playbackPanel.getMediaPlayer().release();
        gui.dispose();
        if (Sync.connectedUsers.size() == 1) {
            server.stop();
        } else {
            closeServer = true;
        }
    }

    public static void sendMessage(String message) {
        messages.add(message);
        ControlPanel.getInstance().setMessages(messages);
    }

    public static void kickUser(User user) {
        Sync.connectedUsers.remove(user);
        ControlPanel.getInstance().updateConnectedClients();
    }

    public static void setEnabled(boolean enabled) {
        gui.setEnabled(enabled);
    }

    class ServerThread implements Runnable {
        private ExecutorService connectionExecutor;
        private GatewayDevice device;
        private ServerSocket service;
        private Socket socket;

        @Override
        public void run() {
            UIMessage message = new UIMessage(gui);
            try {
                device = createGatewayDevice();
                tomcatServer = new TomcatServer();
                new Thread(() -> tomcatServer.start()).start();
                connectionExecutor = Executors.newFixedThreadPool(10);
                service = new ServerSocket(SYNC_PORT);
                isRunning = true;
                gui.setTitle(gui.getTitle() + " (" + ipAddress + ":" + SYNC_PORT + ")");
                gui.setVisible(true);
                new MediaSelectorGUI();
            } catch (IOException e) {
                message.showErrorDialogAndExit(e, "Couldn't open server");
                return;
            }
            try {
                while (isRunning && gui.isVisible()) {
                    socket = service.accept();
                    connectionExecutor.execute(new ServerSocketTask(socket));
                }
            } catch(IOException e) {
                // Couldn't accept a client or on called on server close, just ignore exception.
            } finally {
                connectionExecutor.shutdown();
                while(!connectionExecutor.isTerminated()) {
                    sleep(Duration.ofSeconds(1));
                }
            }
        }

        private GatewayDevice createGatewayDevice() throws IOException {
            GatewayDiscover discover = new GatewayDiscover();
            try {
                discover.discover();
                GatewayDevice device = discover.getValidGateway();
                checkNotNull(device);
                boolean isSyncMapped = device.addPortMapping(
                        SYNC_PORT,
                        SYNC_PORT,
                        device.getLocalAddress().getHostAddress(),
                        "TCP",
                        "sync");
                boolean isTomcatMapped = device.addPortMapping(
                        TOMCAT_PORT,
                        TOMCAT_PORT,
                        device.getLocalAddress().getHostAddress(),
                        "TCP",
                        "tomcat");
                if (isSyncMapped && isTomcatMapped) {
                    Server.ipAddress = device.getExternalIPAddress();
                    return device;
                }
                throw new IOException("Couldn't map port " + SYNC_PORT + " or " + TOMCAT_PORT + " ensure UPnP is enabled");
            } catch (SAXException | ParserConfigurationException e) {
                throw new IOException(e);
            }
        }

        void stop() {
            isRunning = false;
            sleep(Duration.ofSeconds(1));
            try {
                device.deletePortMapping(SYNC_PORT, "TCP");
                device.deletePortMapping(TOMCAT_PORT, "TCP");
            } catch (NullPointerException | IOException | SAXException e) {
                // If this fails, nothing left to do
            }
            try {
                socket.close();
            } catch (IOException | NullPointerException e) {
                // If this fails, nothing left to do
            }
            try {
                service.close();
            } catch (IOException | NullPointerException e) {
                // If this fails, nothing left to do
            }
            if (tomcatServer != null) {
                tomcatServer.stop();
            }
            Launcher.INSTANCE.open();
        }
    }

    class ServerSocketTask implements Runnable {

        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User user;
        private boolean isClientConnected = false;
        private boolean hasConnected = false;
        private ArrayList<String> messages = new ArrayList<>();
        private long lastPingCheck = 0;
        private long lastClientUpdate = 0;
        private long lastMediaUpdate = 0;

        ServerSocketTask(Socket socket) {
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

                if (hasConnected && !Sync.connectedUsers.contains(user)) {
                    disconnectClientFromServer();
                }

                try {
                    if (socket.getInputStream().available() > 0) {
                        Message message = (Message) input.readObject();
                        switch(message.getType()) {
                            case DISCONNECTING:
                                Sync.connectedUsers.remove(user);
                                ControlPanel.getInstance().updateConnectedClients();
                                isClientConnected = false;
                                break;
                            case PING:
                                user.setPing(System.currentTimeMillis() - lastPingCheck);
                                ControlPanel.getInstance().updateConnectedClients();
                                break;
                            case CLIENT_NAME:
                                user = new User(message.getBody().toString());
                                Sync.connectedUsers.add(user);
                                ControlPanel.getInstance().updateConnectedClients();
                                hasConnected = true;
                                break;
                            case MEDIA_URL:
                                user.getMedia().setURL((String) message.getBody());
                                break;
                            case MEDIA_TIME:
                                user.getMedia().setCurrentTime((long) message.getBody());
                                if (GUI.playbackPanel.getMedia().getCurrentTime() != -1) {
                                    long timeDifference = Math.abs(GUI.playbackPanel.getMedia().getCurrentTime()
                                            - user.getMedia().getCurrentTime() + user.getPing());
                                    if (timeDifference > 5000) {
                                        sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_TIME)
                                                .body(GUI.playbackPanel.getMedia().getCurrentTime())
                                                .build());
                                    } else if (timeDifference > 1000) {
                                        float rate = (timeDifference + 1000 + user.getPing()) * 1.0f / 1000;
                                        sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_RATE)
                                                .body(rate >= 0.75f ? rate : 0.75f)
                                                .build());
                                    }
                                }
                                break;
                            case MEDIA_STATE:
                                if (GUI.playbackPanel.getMedia().isPaused() != (boolean) message.getBody()) {
                                    user.getMedia().setPaused(GUI.playbackPanel.getMedia().isPaused());
                                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_STATE)
                                            .body(GUI.playbackPanel.getMedia().isPaused())
                                            .build());
                                }
                                break;
                            case MESSAGES:
                                for (String m : (ArrayList<String>) message.getBody()) {
                                    Server.sendMessage(m);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } catch(IOException | ClassNotFoundException e) {
                    disconnectClientFromServer();
                }

                if (System.currentTimeMillis() > lastPingCheck + 500) {
                    lastPingCheck = System.currentTimeMillis();
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.PING).build());
                }

                if (System.currentTimeMillis() > lastClientUpdate + 1500) {
                    lastClientUpdate = System.currentTimeMillis();
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.CONNECTED_CLIENTS)
                            .body(Sync.connectedUsers)
                            .build());
                }

                if (System.currentTimeMillis() > lastMediaUpdate + 5000
                        || (user != null
                        && !user.getMedia().getURL().equals(GUI.playbackPanel.getMedia().getURL()))) {
                    lastMediaUpdate = System.currentTimeMillis();
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_URL)
                            .body(GUI.playbackPanel.getMedia().getURL())
                            .build());
                }

                if (user != null && GUI.playbackPanel.getMedia().isPaused() != user.getMedia().isPaused()) {
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MEDIA_STATE)
                            .body(GUI.playbackPanel.getMedia().isPaused())
                            .build());
                }

                if (messages.size() < Server.messages.size()) {
                    ArrayList<String> newMessages = new ArrayList<>();
                    ArrayList<String> messageCache = Server.messages;
                    for (int i = messages.size(); i < messageCache.size(); i++) {
                        newMessages.add(messageCache.get(i));
                    }
                    messages.clear();
                    messages.addAll(messageCache);
                    sendMessage(new Message.Builder(Message.MESSAGE_TYPE.MESSAGES)
                            .body(newMessages)
                            .build());
                }
            }

            try {
                socket.close();
            } catch(IOException e) {
                Sync.connectedUsers.remove(user);
                stop();
            }
        }

        void stop() {
            disconnectClientFromServer();
            server.stop();
        }

        private synchronized void connectClientToServer() {
            try {
                input = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) input.readObject();
                isClientConnected = message.getType() == Message.MESSAGE_TYPE.CONNECTING;
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(new Message.Builder(Message.MESSAGE_TYPE.CONNECTED).build());
                output.flush();
            } catch(IOException | ClassNotFoundException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void disconnectClientFromServer() {
            try {
                output.writeObject(new Message.Builder(Message.MESSAGE_TYPE.CLOSING).build());
                output.flush();
            } catch(IOException e) {
                // Do nothing if sending closing message fails
            } finally {
                Sync.connectedUsers.remove(user);
                isClientConnected = false;
            }
        }

        private synchronized void sendMessage(Message message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }
    }

    private void sleep(Duration time) {
       try {
           Thread.sleep(time.toMillis());
       } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
       }
    }
}
