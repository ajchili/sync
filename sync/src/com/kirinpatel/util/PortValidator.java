package com.kirinpatel.util;

import com.kirinpatel.net.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Credit: https://stackoverflow.com/revisions/13826145/3
 */
public class PortValidator {

    public static void isAvailable(int port) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(Server.ipAddress, port), 5000);
        } catch(IOException e) {
            if (port == 8000) {
                new UIMessage("A port is not forwarded!"
                        , "Clients will be unable to connect to your sync server! Please open port " + port + "."
                        , 1);
            } else if (port == 8080) {
                new UIMessage("A port is not forwarded!"
                        , "You will be unable to use offline media! Please open port "
                        + port
                        + " to use offline media."
                        , 1);
            }
        }
    }
}