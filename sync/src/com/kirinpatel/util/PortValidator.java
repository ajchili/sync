package com.kirinpatel.util;

import com.kirinpatel.net.Server;

import java.io.IOException;
import java.net.Socket;

/**
 * Credit: https://stackoverflow.com/revisions/13826145/3
 */
public class PortValidator {

    public static void isAvailable(int port) {
        Debug.Log("Testing connection on port (" + port + ")...", 4);
        Socket s = null;
        try {
            s = new Socket(Server.ipAddress, port);
            Debug.Log("Port (" + port + ") is opened and awaiting connections...", 4);
        } catch(IOException e) {
            Debug.Log("Error testing connection on port (" + port + ").", 5);
            if (port == 8000) new UIMessage("A port is not forwarded!", "Clients will be unable to connect to your sync server! Please open port " + port + ".", 1);
            else new UIMessage("A port is not forwarded!", "You will be unable to use offline media! Please open port " + port + " to use offline media.", 1);
            Server.stop();
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch(IOException e) {
                    Debug.Log("Error testing connection on port (" + port + ").", 5);
                }
            }
        }
    }
}