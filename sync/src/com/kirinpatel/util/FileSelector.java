package com.kirinpatel.util;

import com.kirinpatel.gui.ProgressView;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class FileSelector {

    public static File getFile(Component parent) {
        JFileChooser mediaSelector = new JFileChooser("tomcat/webapps/media");
        mediaSelector.setFileSelectionMode(JFileChooser.FILES_ONLY);
        mediaSelector.showOpenDialog(parent);

        File selectedFile = mediaSelector.getSelectedFile();
        if (selectedFile == null) return null;

        if (selectedFile.getAbsolutePath().startsWith(new File("tomcat/webapps/media").getAbsolutePath())) {
            return selectedFile;
        } else {
            return moveFile(selectedFile);
        }
    }

    /**
     * Credit: https://www.mkyong.com/java/how-to-move-file-to-another-directory-in-java/
     *
     * @param selectedFile
     * @return
     */
    private static File moveFile(File selectedFile) {
        File newFile = new File("tomcat/webapps/media/" + selectedFile.getName());

        ProgressView progressView = new ProgressView("Moving media", "Please wait while your media is moved to the proper folder.");
        new Thread(() -> {
            try {
                InputStream inStream = new FileInputStream(selectedFile);
                OutputStream outStream = new FileOutputStream(newFile);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                inStream.close();
                outStream.close();

            } catch(IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            while(newFile.length() < selectedFile.length()) {
                progressView.setProgress(newFile.length(), selectedFile.length());
            }

            progressView.dispose();
            new UIMessage("Your media is ready", "Your media has been moved to the\nTomcat folder and is ready for playback.", 0);
        }).start();

        return newFile;
    }
}
