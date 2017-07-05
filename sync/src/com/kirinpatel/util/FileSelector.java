package com.kirinpatel.util;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class FileSelector {

    public static File getFile(Component parent) {
        JFileChooser mediaSelector = new JFileChooser("tomcat/webapps/media");
        mediaSelector.setFileSelectionMode(JFileChooser.FILES_ONLY);
        mediaSelector.showOpenDialog(parent);

        File selectedFile = mediaSelector.getSelectedFile();
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

        try {
            InputStream inStream = new FileInputStream(selectedFile);
            OutputStream outStream = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];
            int length;

            new UIMessage("Copied media", "Your media is being copied to the Tomcat folder.\nDuring this time, the application will be unresponsive, however,\nit should not take long.", 0);
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            new UIMessage("Media copied successfully", "Your media has been copied to the Tomcat folder.", 0);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return newFile;
    }
}
