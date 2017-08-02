package com.kirinpatel;

import com.kirinpatel.net.User;
import com.kirinpatel.util.UIMessage;
import jdk.nashorn.api.scripting.URLReader;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public final class sync {

    private final static int VERSION = 1;
    private final static int BUILD = 6;
    private final static int REVISION = 0;
    public static long deSyncWarningTime = 1000;
    public static long deSyncTime = 5000;
    public static boolean showUserTimes = false;
    public static ArrayList<User> connectedUsers = new ArrayList<>();

    public static void main(String[] args) {
        if (isUpdated()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e) {
                UIMessage.showErrorDialog(e, "Unable to set look and feel of sync.");
            } finally {
                if (verifyDependencies()) {
                    Launcher.setInstance();
                }
            }
        } else {
            UIMessage.showMessageDialog(
                    "You have an outdated version of sync, please update sync!",
                    "Outdated version of sync.");
        }
    }

    private static boolean verifyDependencies() {
        if (!new NativeDiscovery().discover()) {
            UIMessage.showErrorDialog(new IllegalAccessException("Unable to load VLCJ." +
                            "\nPlease ensure that both VLC and Java are installed and use the same bit mode (32 or 64 bit)."),
                    "Unable to launch sync.");
            return false;
        }
        return true;
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

            return !(sync.VERSION != version[0]
                    || sync.BUILD < version[1]
                    || sync.REVISION < version[2] && sync.BUILD == version[1]);
        } catch(MalformedURLException e) {
            UIMessage.showErrorDialog(e, "Unable to verify version");
            return false;
        }
    }
}
