package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.utils.FileSelector;
import com.kirinpatel.sync.net.Media;
import com.kirinpatel.sync.utils.UIMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.nio.file.Paths;

import static com.kirinpatel.sync.gui.MediaSelectorGUI.MEDIA_TYPE.*;

public class MediaSelectorGUI extends JFrame {

    public enum MEDIA_TYPE {
        OFFLINE(0),
        ONLINE(1);

        private int mediaType;

        MEDIA_TYPE(int mediaType) {
            this.mediaType = mediaType;
        }

        public int getMediaType() {
            return mediaType;
        }
    }

    // Prevents multiple MediaSelectorGUIs
    private static boolean isOpened = false;

    public MediaSelectorGUI() {
        super("sync");

        if (isOpened) {
            return;
        }

        setSize(new Dimension(200, 100));
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {
                dispose();
                isOpened = false;
            }
        });
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        JButton onlineMedia = new JButton("Set Media URL");
        onlineMedia.addActionListener(new MediaButtonEvent(ONLINE));
        buttonPanel.add(onlineMedia);
        JButton offlineMedia = new JButton("Set Media File");
        offlineMedia.addActionListener(new MediaButtonEvent(OFFLINE));
        buttonPanel.add(offlineMedia);
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
        isOpened = true;
    }

    class MediaButtonEvent implements ActionListener {

        private MEDIA_TYPE type;

        MediaButtonEvent(MEDIA_TYPE type) {
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);

            switch(type) {
                case ONLINE:
                    String mediaURL = UIMessage.getInput("Set media URL", "Please provide the media URL of your media.");
                    if (mediaURL != null && !mediaURL.isEmpty()) {
                        if (mediaURL.startsWith("http")) {
                            GUI.playbackPanel.getMediaPlayer().setMediaSource(new Media(mediaURL));
                        } else {
                            GUI.playbackPanel.getMediaPlayer().setMediaSource(new Media("http://" + mediaURL));
                        }
                    } else {
                        if (mediaURL != null) {
                            UIMessage.showMessageDialog(
                                    "The Media URL must be specified!",
                                    "Error setting Media URL!");
                        }
                    }
                    break;
                case OFFLINE:
                    File mediaFile = FileSelector.getFile(null);
                    if (mediaFile != null && mediaFile.getAbsolutePath().startsWith(new File("tomcat/webapps/media").getAbsolutePath())) {
                        GUI.playbackPanel.getMediaPlayer().setMediaSource(new Media(Paths.get(mediaFile.getAbsolutePath())));
                    } else {
                        if (mediaFile != null) {
                            UIMessage.showMessageDialog(
                                    "The media file that you selected could not be used.",
                                    "Error selecting media!");
                        }
                    }
                    break;
            }
        }
    }
}
