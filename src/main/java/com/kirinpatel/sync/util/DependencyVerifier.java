package com.kirinpatel.sync.util;

import java.io.IOException;
import java.net.URL;

public class DependencyVerifier {

    private static final boolean IS_64_BIT = System.getProperty("os.arch").contains("64");
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void downloadDependencies() {
        UIMessage.showMessageDialog("The correct versions of Java and VLC were unable to be found on\n" +
                "your computer. They will be downloaded and ran for you. Please\n" +
                "allow them to be run.", "Downloading dependencies");
        if (OS.contains("win")) {
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
                            new URL("https://ftp.osuosl.org/pub/videolan/vlc/2.2.8/win64/vlc-2.2.8-win64.exe")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (OS.contains("mac")) {

        } else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {

        } else {
            UIMessage.showErrorDialog(new IOException("Unable to determine operating system." +
                            "\nYou must manually download dependencies."),
                    "Error downloading dependencies.");
        }
    }

    private static void runAsAdmin(String filePath) throws IOException {
        Runtime.getRuntime().exec("cmd /c start " + filePath);
    }
}
