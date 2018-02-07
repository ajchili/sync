package com.kirinpatel.sync.util;

import java.io.IOException;
import java.net.URL;

public class DependencyVerifier {

    private static final boolean IS_64_BIT = System.getProperty("os.arch").contains("64");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String VLC_VERSION = "2.2.8";

    public static void downloadDependencies() {
        if (OS.contains("win")) {
            UIMessage.showMessageDialog("The correct version(s) of Java/VLC were unable to be found on\n" +
                    "your computer. They will be downloaded and ran for you. Please\n" +
                    "allow them to be run.", "Downloading dependencies");
            new Thread(() -> {
                if (!IS_64_BIT){
                    try {
                        runAsAdmin(FileDownloader.downloadFile("Java.exe",
                                new URL("http://javadl.oracle.com/webapps/download/AutoDL?BundleId=230542_" +
                                        "2f38c3b165be4555a1fa6e98c45e0808")));
                    } catch (IOException e) {
                        UIMessage.showErrorDialog(e, "Unable to download Java");
                    }
                }
            }).start();

            new Thread(() -> {
                try {
                    runAsAdmin(FileDownloader.downloadFile("VLC.exe",
                            new URL("https://ftp.osuosl.org/pub/videolan/vlc/"
                                    + VLC_VERSION
                                    + "/win64/vlc-"
                                    + VLC_VERSION
                                    + "-win64.exe")));
                } catch (IOException e) {
                    UIMessage.showErrorDialog(e, "Unable to download VLC");
                }
            }).start();
        } else if (OS.contains("mac")) {
            UIMessage.showMessageDialog("The correct version(s) of Java/VLC were unable to be found on\n" +
                    "your computer. They will be downloaded however, you will\n" +
                    "have to manually run them.", "Downloading dependencies");
            new Thread(() -> {
                try {
                    runDMG(FileDownloader.downloadFile("VLC.dmg",
                            new URL("https://mirror.clarkson.edu/videolan/vlc/"
                                    + VLC_VERSION
                                    + "/macosx/vlc-"
                                    + VLC_VERSION
                                    + ".dmg")));
                } catch (IOException e) {
                    UIMessage.showErrorDialog(e, "Unable to download VLC");
                }
            }).start();
        } else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            UIMessage.showErrorDialog(
                    new IllegalStateException("Unable to load VLCJ or Java." +
                            "\nPlease ensure that both VLC and Java are installed and are the same " +
                            "(32 or 64 bit depending on your system)."),
                    "Unable to launch sync");
        } else {
            UIMessage.showErrorDialog(new IOException("Unable to determine operating system." +
                            "\nYou must manually download dependencies."),
                    "Error downloading dependencies.");
        }
    }

    private static void runAsAdmin(String filePath) throws IOException {
        Runtime.getRuntime().exec("cmd /c start " + filePath);
    }

    private static void runDMG(String filePath) throws IOException {
        new ProcessBuilder("open", "-R", filePath).start();
    }
}
