package com.kirinpatel.sync.net;

import com.kirinpatel.sync.gui.Launcher;
import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.gui.ControlPanel;
import com.kirinpatel.sync.gui.GUI;
import com.kirinpatel.sync.gui.MediaSelectorGUI;
import com.kirinpatel.sync.util.Message;
import com.kirinpatel.sync.util.UIMessage;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;
import static com.kirinpatel.sync.util.Message.MESSAGE_TYPE.*;

public class Server implements NetworkUser {

    public static String ipAddress = "";
    public static GUI gui;
    public static User user;
    private ServerThread server;
    private TomcatServer tomcatServer;
    private ArrayList<String> messages;
    private boolean isRunning = false;
    private boolean closeServer = false;

    private final static int SYNC_PORT = 8000;
    final static int TOMCAT_PORT = 8080;

    public Server() {
        Launcher.connectedUser = this;
        gui = new GUI(SERVER);
        user = new User(System.getProperty("user.name") + " (host)");
        Sync.connectedUsers.add(user);
        Sync.host = Sync.connectedUsers.get(0);
        messages = new ArrayList<>();
        ControlPanel.getInstance().setMessages(messages);
        ControlPanel.getInstance().updateConnectedClients();
        server = new ServerThread();
        new Thread(server).start();
    }

    public Server(String mediaURL) {
        Launcher.connectedUser = this;
        gui = new GUI(SERVER);
        user = new User(System.getProperty("user.name") + " (host)");
        Sync.connectedUsers.add(user);
        Sync.host = Sync.connectedUsers.get(0);
        ControlPanel.getInstance().updateConnectedClients();
        server = new ServerThread();
        gui.playbackPanel.getMediaPlayer().setMediaSource(new Media(mediaURL));
        new Thread(server).start();
    }

    @Override
    public void stop() {
        gui.playbackPanel.getMediaPlayer().release();
        gui.dispose();
        if (Sync.connectedUsers.size() == 1) {
            server.stop();
        } else {
            closeServer = true;
        }
    }

    @Override
    public void sendMessage(@NotNull String message) {
        messages.add(message);
        ControlPanel.getInstance().setMessages(messages);
    }

    public static void kickUser(User user) {
        Sync.connectedUsers.remove(user);
        ControlPanel.getInstance().updateConnectedClients();
    }

    public GUI getGUI() {
        return gui;
    }

    public static void setEnabled(boolean enabled) {
        gui.setEnabled(enabled);
    }

    @Override
    public User getUser() {
        return user;
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
                connectionExecutor = Executors.newCachedThreadPool();
                service = new ServerSocket(SYNC_PORT);
                isRunning = true;
                SwingUtilities.invokeLater(() -> {
                    gui.setVisible(true);
                    new MediaSelectorGUI(gui);
                });
            } catch (Exception e) {
                message.showErrorDialogAndExit(e, "Couldn't open server");
                Server.this.stop();
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

        private GatewayDevice createGatewayDevice() {
            GatewayDiscover discover = new GatewayDiscover();
            try {
                discover.discover();
                GatewayDevice device = discover.getValidGateway();
                if (device == null) {
                    UIMessage.showErrorDialog(new IOException("Couldn't map port "
                                    + SYNC_PORT
                                    + " or "
                                    + TOMCAT_PORT
                                    + " to use UPnP, please ensure that it is enabled" +
                                    "\nin your router. Otherwise you will have to manually port forward."),
                            "Error establishing UPnP");
                    getIPAddress();
                    return null;
                }
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
            } catch (SAXException | ParserConfigurationException | IOException e) {
                UIMessage.showErrorDialog(new IOException("Couldn't map port "
                        + SYNC_PORT
                        + " or "
                        + TOMCAT_PORT
                        + "\nto use UPnP please ensure"
                        + "that it is enabled in your router." +
                        "\nOtherwise you will have to manually port forward."),
                        "Error establishing UPnP");
            }
            return null;
        }

        private void getIPAddress() {
            try {
                URL url = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                Server.ipAddress = in.readLine();
            } catch (IOException e) {
                UIMessage.showErrorDialog(e, "Unable to obtain your IP Address.");
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
            new Launcher();
        }
    }

    class ServerSocketTask implements Runnable {

        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User user;
        private boolean isClientConnected = false;
        private boolean hasConnected = false;
        private ArrayList<String> clientMessages = new ArrayList<>();
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
                        switch(message.getMessageType()) {
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
                                user = new User(message.getMessage().toString());
                                Sync.connectedUsers.add(user);
                                ControlPanel.getInstance().updateConnectedClients();
                                hasConnected = true;
                                break;
                            case MEDIA_URL:
                                user.getMedia().setURL((String) message.getMessage());
                                break;
                            case MEDIA_TIME:
                                user.getMedia().setCurrentTime((long) message.getMessage());
                                if (gui.playbackPanel.getMedia().getCurrentTime() != -1) {
                                    sendMediaTime();
                                }
                                break;
                            case MEDIA_STATE:
                                if (gui.playbackPanel.getMedia().isPaused() != (boolean) message.getMessage()) {
                                    sendMediaState();
                                }
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

                if (System.currentTimeMillis() > lastPingCheck + 500) {
                    sendPing();
                }

                if (System.currentTimeMillis() > lastClientUpdate + 1500) {
                    sendConnectedUsersToClient();
                }

                if (System.currentTimeMillis() > lastMediaUpdate + 5000
                        || (user != null
                        && !user.getMedia().getURL().equals(gui.playbackPanel.getMedia().getURL()))) {
                    sendMediaURL();
                }

                if (user != null && gui.playbackPanel.getMedia().isPaused() != user.getMedia().isPaused) {
                    sendMediaState();
                }

                if (clientMessages.size() < messages.size()) {
                    sendMessagesToClient();
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
                isClientConnected = message.getMessageType() == CONNECTING
                        && message.getMessage().toString().equals(Sync.VERSION);
                output = new ObjectOutputStream(socket.getOutputStream());
                if (isClientConnected) {
                    output.writeObject(new Message(CONNECTED, null));
                } else {
                    output.writeObject(new Message(ERROR, Sync.VERSION));
                }
                output.flush();
            } catch(IOException | ClassNotFoundException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void disconnectClientFromServer() {
            try {
                output.writeObject(new Message(CLOSING, null));
                output.flush();
            } catch(IOException e) {
                // Do nothing if sending closing message fails
            } finally {
                Sync.connectedUsers.remove(user);
                isClientConnected = false;
            }
        }

        private synchronized void sendPing() {
            try {
                lastPingCheck = System.currentTimeMillis();
                output.writeObject(new Message(PING, null));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendConnectedUsersToClient() {
            try {
                lastClientUpdate = System.currentTimeMillis();
                output.reset();
                output.writeObject(new Message(CONNECTED_CLIENTS, Sync.connectedUsers));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendMessagesToClient() {
            try {
                ArrayList<String> newMessages = new ArrayList<>();
                ArrayList<String> messageCache = messages;
                for (int i = clientMessages.size(); i < messageCache.size(); i++) {
                    newMessages.add(messageCache.get(i));
                }
                clientMessages.clear();
                clientMessages.addAll(messageCache);
                output.flush();
                output.writeObject(new Message(MESSAGES, newMessages));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendMediaURL() {
            try {
                lastMediaUpdate = System.currentTimeMillis();
                output.flush();
                output.writeObject(new Message(MEDIA_URL, gui.playbackPanel.getMedia().getURL()));
                output.flush();
            } catch(IOException e) {
                disconnectClientFromServer();
            }
        }

        private synchronized void sendMediaTime() {
            long timeDifference = Math.abs(gui.playbackPanel.getMedia().getCurrentTime()
                    - user.getMedia().currentTime + user.getPing());
            if (timeDifference > 5000) {
                try {
                    output.writeObject(new Message(MEDIA_TIME, gui.playbackPanel.getMedia().getCurrentTime()));
                    output.flush();
                } catch(IOException e) {
                    disconnectClientFromServer();
                }
            } else if (timeDifference > 1000) {
                try {
                    float rate = (timeDifference + 1000 + user.getPing()) * 1.0f / 1000;
                    output.writeObject(new Message(MEDIA_RATE, rate >= 0.75f ? rate : 0.75f));
                    output.flush();
                } catch(IOException e) {
                    disconnectClientFromServer();
                }
            }
        }

        private synchronized void sendMediaState() {
            try {
                user.getMedia().isPaused = gui.playbackPanel.getMedia().isPaused();
                output.writeObject(new Message(MEDIA_STATE, gui.playbackPanel.getMedia().isPaused()));
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
