package com.kirinpatel.sync;

import com.kirinpatel.sync.gui.Launcher;
import com.kirinpatel.sync.net.User;
import com.kirinpatel.sync.util.DependencyVerifier;
import com.kirinpatel.sync.util.UIMessage;
import jdk.nashorn.api.scripting.URLReader;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public final class Sync {

    public final static String VERSION = "2.0.0B";
    public static ArrayList<User> connectedUsers = new ArrayList<>();
    public static User host;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
            } catch(Exception e) {
                UIMessage.showErrorDialog(e, "Unable to set look and feel of sync");
            } finally {
                verifyDependencies();
            }
        });
    }

    private static void verifyDependencies() {
        if (!new NativeDiscovery().discover()) {
            try {
                DependencyVerifier.downloadDependencies();
            } catch (IOException e) {
                UIMessage.showErrorDialog(e, "Unable to download dependencies");
            }
        } else {
            new Launcher();
            checkVersion();
        }
    }

    private static void checkVersion() {
        String version = "";
        String beta = "";
        try {
            Scanner s = new Scanner(new URLReader(
                    new URL("https://raw.githubusercontent.com/ajchili/sync/master/VERSION")));
            while(s.hasNext()) {
                String currentLine = s.nextLine();
                if (currentLine.contains("B")) {
                    beta = currentLine;
                } else {
                    version = currentLine;
                }
            }
        } catch (MalformedURLException  e) {
            // Show error but allow usage of sync
            UIMessage.showErrorDialog(
                    new IllegalStateException("The version file was unable to be verified, please check the Github\n" +
                            "page to verify that you have the most recent version of sync."),
                    "Unable to verify current version of sync");
        }

        if (!VERSION.contains("B") && !version.equals(VERSION)) {
            UIMessage.showMessageDialog(
                    "You have an outdated version of sync, please update sync!",
                    "Outdated version of sync");
        } else if (VERSION.contains("B") && !beta.equals(VERSION)) {
            UIMessage.showMessageDialog(
                        "A new beta has been released, please update to fix bugs and test new features!",
                        "Outdated version of sync");
        } else {
            checkForMessage();
        }
    }

    private static void checkForMessage() {
        String message = "";
        try {
            Scanner s = new Scanner(new URLReader(
                    new URL("https://raw.githubusercontent.com/ajchili/sync/master/MESSAGE")));
            while(s.hasNext()) {
                message = s.nextLine();
            }
        } catch (MalformedURLException  e) {
            // Do nothing if url was unable to be loaded
        }

        if (message.length() > 0) {
            UIMessage.showMessageDialog(message,"News");
        }
    }
}
