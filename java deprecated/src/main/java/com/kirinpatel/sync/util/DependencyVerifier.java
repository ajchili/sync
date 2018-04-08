package com.kirinpatel.sync.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DependencyVerifier {

    private static final boolean IS_64_BIT = System.getProperty("os.arch").contains("64");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String[] VLC_DOWNLOAD_LINKS = {
            "https://ftp.osuosl.org/pub/videolan/vlc/2.2.8/win64/vlc-2.2.8-win64.exe",
            "https://ftp.osuosl.org/pub/videolan/vlc/2.2.8/macosx/vlc-2.2.8.dmg"
    };

    public static void downloadDependencies() throws IOException {
        UIMessage.showMessageDialog("The correct version(s) of Java/VLC were unable to be found on\n" +
                "your computer. They will be downloaded and ran for you. Please\n" +
                "allow them to be run.", "Downloading dependencies");

        if (OS.contains("win")) {
            if (!IS_64_BIT){
                runAsAdmin(downloadFile("Java.exe",
                        new URL("http://javadl.oracle.com/webapps/download/AutoDL?BundleId=230542_" +
                                "2f38c3b165be4555a1fa6e98c45e0808")));
            }


            runAsAdmin(downloadFile("VLC.exe", new URL(VLC_DOWNLOAD_LINKS[0])));
        } else if (OS.contains("mac")) {
            runDMG(downloadFile("VLC.dmg", new URL(VLC_DOWNLOAD_LINKS[1])));
        }
        /*
            Linux dependency downloading will be unsupported until a system can be used to test/develop on.

            else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
                UIMessage.showErrorDialog(
                        new IllegalStateException("Unable to load VLCJ or Java." +
                                "\nPlease ensure that both VLC and Java are installed and are the same " +
                                "(32 or 64 bit depending on your system)."),
                        "Unable to launch sync");
            }
        */
        else {
            UIMessage.showErrorDialog(new IOException("Unable to determine operating system." +
                            "\nYou must manually download dependencies."),
                    "Error downloading dependencies.");
        }
    }

    @NotNull
    private static String downloadFile(String name, URL url)throws IOException {
        try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(name);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        } catch (IOException e) {
            throw new IOException("File unable to download!", e);
        }
        return new File(name).getAbsolutePath();
    }

    private static void runAsAdmin(String filePath) throws IOException {
        Runtime.getRuntime().exec("cmd /c start " + filePath);
    }

    private static void runDMG(String filePath) throws IOException {
        new ProcessBuilder("open", "-R", filePath).start();
    }
}
