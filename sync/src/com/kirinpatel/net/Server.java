package com.kirinpatel.net;

import com.kirinpatel.gui.ServerGUI;
import com.kirinpatel.util.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kirin Patel
 * @version 0.0.1
 * @date 6/16/17
 */
public class Server {

    private ServerGUI gui;
    private ArrayList<User> connectedClients = new ArrayList<>();
    private static ServerThread server;
    private static boolean closeConnections = false;

    public Server() {
        gui = new ServerGUI();

        connectedClients.add(new User(System.getProperty("user.name")));
        gui.serverControlPanel.updateConnectedClients(connectedClients);

        server = new ServerThread();
        new Thread(server).start();
    }

    public static void stop() {
        closeConnections = true;
        server.stop();
    }

    class ServerThread implements Runnable {

        private ExecutorService connectionExecutor;
        private ServerSocket service;
        private Socket socket;
        private boolean isRunning = false;

        public void run() {
            start();

            connectionExecutor = Executors.newFixedThreadPool(10);

            try {
                service = new ServerSocket(8000);
                service.setReuseAddress(true);

                while(isRunning) {
                    socket = service.accept();

                    connectionExecutor.execute(new ServerSocketTask(socket));
                }
            } catch (IOException e) {
                // TODO: Fix issue where socket is closed and crashes program
                // e.printStackTrace();
            } finally {
                connectionExecutor.shutdown();

                while (!connectionExecutor.isTerminated()) {

                }
            }
        }

        public void start() {
            isRunning = true;
        }

        public void stop() {
            isRunning = false;

            if (service != null) {
                if (socket != null) {
                    if (!socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (!service.isClosed()) {
                    try {
                        service.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class ServerSocketTask implements Runnable {

        private Socket socket;
        private boolean isClientConnected = false;

        public ServerSocketTask(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            isClientConnected = true;

            while(isClientConnected) {
                if (closeConnections) {
                    stop();
                }
            }
        }

        public void start() {
            isClientConnected = true;
        }

        public void stop() {
            isClientConnected = false;
        }
    }
}
