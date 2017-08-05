package com.kirinpatel.sync;

import com.kirinpatel.sync.net.User;
import com.kirinpatel.sync.util.UIMessage;
import jdk.nashorn.api.scripting.URLReader;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public final class Sync {

    private final static int VERSION = 1;
    private final static int BUILD = 6;
    private final static int REVISION = 0;
    public static ArrayList<User> connectedUsers = new ArrayList<>();
    public static User host;

    public static void main(String[] args) {
        if (isUpdated()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(IllegalAccessException
                    | InstantiationException
                    | UnsupportedLookAndFeelException
                    | ClassNotFoundException e) {
                UIMessage.showErrorDialog(e, "Unable to set look and feel of sync.");
            }
            verifyDependencies();
        } else {
            UIMessage.showMessageDialog(
                    "You have an outdated version of sync, please update sync!",
                    "Outdated version of sync.");
        }
    }

    private static void verifyDependencies() {
        if (!new NativeDiscovery().discover()) {
            UIMessage.showErrorDialog(
                    new IllegalStateException("Unable to load VLCJ or Java." +
                            "\nPlease ensure that both VLC and Java are installed and are the same " +
                            "(32 or 64 bit depending on your system)."),
                    "Unable to launch sync.");
        } else {
            Launcher.INSTANCE.open();
        }
    }

    private static boolean isUpdated() {
        try {
            Scanner s = new Scanner(new URLReader(new URL("https://github.com/ajchili/sync/releases")));
            int[] version = new int[3];
            while(s.hasNext()) {
                String line = s.nextLine();
                if (line.contains("tag-reference")) {
                    s.nextLine();
                    String release;
                    release = s.nextLine();
                    release = release.substring(release.indexOf("tree/") + 5, release.indexOf("tree/") + 10);
                    version = new int[]{
                            Integer.parseInt(release.substring(0, release.indexOf('.'))),
                            Integer.parseInt(release.substring(release.indexOf('.') + 1, release.lastIndexOf('.'))),
                            Integer.parseInt(release.substring(release.lastIndexOf('.') + 1))
                    };
                    break;
                }
            }

            return !(Sync.VERSION != version[0]
                    || Sync.BUILD < version[1]
                    || Sync.REVISION < version[2] && Sync.BUILD == version[1]);
        } catch(MalformedURLException e) {
            UIMessage.showErrorDialog(e, "Unable to verify version");
            return false;
        }
    }
}
