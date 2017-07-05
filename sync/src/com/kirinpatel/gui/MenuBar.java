package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.FileSelector;
import com.kirinpatel.util.UIMessage;
import com.kirinpatel.util.URLEncoding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class MenuBar extends JMenuBar {

    public MenuBar(PlaybackPanel playbackPanel) {
        super();

        JMenu sync = new JMenu("sync");
        JMenu file = new JMenu("Set Media");
        JMenuItem setURL = new JMenuItem("Set Media URL");
        setURL.addActionListener(e -> {
            String mediaURL = UIMessage.getInput("Set media URL", "Please provide the media URL of your media.");
            if (!mediaURL.isEmpty()) {
                if (mediaURL.startsWith("http")) PlaybackPanel.mediaPlayer.setMediaURL(mediaURL);
                else PlaybackPanel.mediaPlayer.setMediaURL("http://" + mediaURL);
            } else {
                if (!PlaybackPanel.mediaPlayer.getMediaURL().isEmpty()) PlaybackPanel.mediaPlayer.setMediaURL("");
                else {
                    Debug.Log("Media URL not specified!", 2);
                    new UIMessage("Error setting Media URL!", "The Media URL must be specified!", 1);
                }
            }
        });
        file.add(setURL);
        JMenuItem setFile = new JMenuItem("Set Media File");
        setFile.addActionListener(e -> {
            File mediaFile = FileSelector.getFile(this);
            if (mediaFile != null && mediaFile.getAbsolutePath().startsWith(new File("tomcat/webapps/media").getAbsolutePath())) {
                String url = "http://" + Server.ipAddress + ":8080/";
                String fileName = mediaFile.getName();
                if (playbackPanel.type == 0) PlaybackPanel.mediaPlayer.setMediaFile(mediaFile.getAbsolutePath(), url + URLEncoding.encode(fileName));
                else PlaybackPanel.mediaPlayer.setMediaURL(url + fileName);
            } else {
                if (!mediaFile.getAbsolutePath().startsWith(new File("tomcat/webapps/media").getAbsolutePath())) {

                } else new UIMessage("Error selecting media!", "The media file that you selected could not be used.\nPlease make sure that it is inside of the media directory.", 1);
            }
        });
        file.add(setFile);
        if (playbackPanel.type == 0) {
            sync.add(file);
            sync.add(new JSeparator());
        }
        JMenuItem close = new JMenuItem("Close sync");
        sync.add(close);
        add(sync);

        JMenu settings = new JMenu("Settings");
        JMenu ui = new JMenu("Interface Settings");
        JMenuItem fullscreen = new JMenuItem("Launch Fullscreen");
        fullscreen.addActionListener(e -> {
            playbackPanel.initFullscreen();
        });
        ui.add(fullscreen);
        settings.add(ui);
        JMenu controlPanel = new JMenu("Control Panel");
        ButtonGroup controlPanelButtons = new ButtonGroup();
        JRadioButtonMenuItem showControlPanel = new JRadioButtonMenuItem("Show");
        showControlPanel.addActionListener(e -> {
            playbackPanel.getParent().add(playbackPanel.type == 0 ? ServerGUI.controlPanel : ClientGUI.controlPanel, BorderLayout.EAST);
            playbackPanel.getParent().repaint();
        });
        showControlPanel.setSelected(true);
        controlPanel.add(showControlPanel);
        JRadioButtonMenuItem hideControlPanel = new JRadioButtonMenuItem("Hide");
        hideControlPanel.addActionListener(e -> {
            playbackPanel.getParent().remove(playbackPanel.type == 0 ? ServerGUI.controlPanel : ClientGUI.controlPanel);
            playbackPanel.getParent().repaint();
        });
        controlPanel.add(hideControlPanel);
        controlPanelButtons.add(showControlPanel);
        controlPanelButtons.add(hideControlPanel);
        ui.add(controlPanel);
        JMenu video = new JMenu("Video Settings");
        JMenuItem videoSettings = new JMenuItem("Show Video Settings Window");
        video.add(videoSettings);
        JMenu videoRenderQuality = new JMenu("Video Render Quality");
        JSlider qualitySlider = new JSlider(1, 100, Main.videoQuality);
        qualitySlider.addChangeListener(e -> {
            Main.videoQuality = ((JSlider) e.getSource()).getValue();
        });
        qualitySlider.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                String ipAddress = Client.ipAddress;
                Client.stop();
                new Client(ipAddress);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        videoRenderQuality.add(qualitySlider);
        if (playbackPanel.type == 2) {
            video.add(new JSeparator());
            video.add(videoRenderQuality);
        }
        // settings.add(video);
        JMenu audio = new JMenu("Audio Settings");
        JMenuItem audioSettings = new JMenuItem("Show Audio Settings Window");
        audio.add(audioSettings);
        // settings.add(audio);
        add(settings);
    }
}
