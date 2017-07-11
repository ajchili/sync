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
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class MenuBar extends JMenuBar {

    public MenuBar(PlaybackPanel playbackPanel) {
        super();

        /**
         * Application section
         */
        JMenu sync = new JMenu("sync");

            /**
             * Media section
             */
            JMenu file = new JMenu("Set Media");
            JMenuItem setURL = new JMenuItem("Set Media URL");
            setURL.addActionListener(e -> {
                String mediaURL = UIMessage.getInput("Set media URL", "Please provide the media URL of your media.");
                if (mediaURL != null && !mediaURL.isEmpty()) {
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
            if (!Main.IS_MAC) file.add(setFile);
            if (playbackPanel.type == 0) {
                sync.add(file);
                sync.add(new JSeparator());
            }

            /**
             * Share section
             */
            JMenuItem share = new JMenuItem("Share Server Address");
            share.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(playbackPanel.type == 0 ? Server.ipAddress : Client.ipAddress), null);
                new UIMessage("Server Address Copied", "The server address has been copied to your clipboard.", 0);
            });
            sync.add(share);
            sync.add(new JSeparator());

            /**
             * Close section
             */
            JMenuItem close = new JMenuItem("Close sync");
            close.addActionListener(e -> {
                if (playbackPanel.type == 0) Server.stop();
                else Client.stop();
            });
            sync.add(close);

        add(sync);

        /**
         * Settings section
         */
        JMenu settings = new JMenu("Settings");

            /**
             * Interface settings section
             */
            JMenu ui = new JMenu("Interface Settings");

                /**
                 * Fullscreen
                 */
                JMenuItem fullscreen = new JMenuItem("Launch Fullscreen");
                fullscreen.addActionListener(e -> {
                    playbackPanel.initFullscreen();
                });
                ui.add(fullscreen);
                ui.add(new JSeparator());

                /**
                 * Control Panel
                 */
                JMenu controlPanel = new JMenu("Control Panel");
                JMenu controlPanelLocation = new JMenu("Location");
                ButtonGroup locationControlPanelButtons = new ButtonGroup();
                JRadioButtonMenuItem rightControlPanel = new JRadioButtonMenuItem("Display on Right");
                rightControlPanel.addActionListener(e -> {
                    if (playbackPanel.getParent().getComponents().length == 2) {
                        playbackPanel.getParent().remove(GUI.controlPanel);
                        playbackPanel.getParent().add(GUI.controlPanel, BorderLayout.EAST);
                        playbackPanel.getParent().revalidate();
                        playbackPanel.getParent().repaint();
                    }
                });
                rightControlPanel.setSelected(true);
                controlPanelLocation.add(rightControlPanel);
                JRadioButtonMenuItem leftControlPanel = new JRadioButtonMenuItem("Display on Left");
                leftControlPanel.addActionListener(e -> {
                    if (playbackPanel.getParent().getComponents().length == 2) {
                        playbackPanel.getParent().remove(GUI.controlPanel);
                        playbackPanel.getParent().add(GUI.controlPanel, BorderLayout.WEST);
                        playbackPanel.getParent().revalidate();
                        playbackPanel.getParent().repaint();
                    }
                });
                controlPanelLocation.add(leftControlPanel);
                locationControlPanelButtons.add(rightControlPanel);
                locationControlPanelButtons.add(leftControlPanel);
                controlPanel.add(controlPanelLocation);
                JMenu mediaTime = new JMenu("Playback Times");
                JMenu mediaTimeIndicators = new JMenu("Playback Time Indicator");
                JPanel warningTimePanel = new JPanel(new BorderLayout());
                warningTimePanel.add(new JLabel("Warning Time: ", JLabel.LEFT), BorderLayout.WEST);
                JSlider warningSlider = new JSlider(250, 1999, 1000);
                warningSlider.setPaintTicks(true);
                warningSlider.setMajorTickSpacing(100);
                warningSlider.setMinorTickSpacing(50);
                warningSlider.setToolTipText("Display desync warning after " + warningSlider.getValue() / 1000.0f + " seconds");
                warningSlider.addChangeListener(e -> {
                    Debug.deSyncWarningTime = warningSlider.getValue();
                    warningSlider.setToolTipText("Display desync warning after " + warningSlider.getValue() / 1000.0f + " seconds");
                });
                warningTimePanel.add(warningSlider, BorderLayout.CENTER);
                mediaTimeIndicators.add(warningTimePanel);
                JPanel desyncTimePanel = new JPanel(new BorderLayout());
                desyncTimePanel.add(new JLabel("DeSync Time: ", JLabel.LEFT), BorderLayout.WEST);
                JSlider desyncSlider = new JSlider(2000, 10000, 2000);
                desyncSlider.setPaintTicks(true);
                desyncSlider.setMajorTickSpacing(500);
                desyncSlider.setMinorTickSpacing(250);
                desyncSlider.setToolTipText("Display desync after " + desyncSlider.getValue() / 1000.0f + " seconds");
                desyncSlider.addChangeListener(e -> {
                    Debug.deSyncWarningTime = desyncSlider.getValue();
                    desyncSlider.setToolTipText("Display desync after " + desyncSlider.getValue() / 1000.0f + " seconds");
                });
                desyncTimePanel.add(desyncSlider, BorderLayout.CENTER);
                mediaTimeIndicators.add(desyncTimePanel);
                mediaTime.add(mediaTimeIndicators);
                mediaTime.add(new JSeparator());
                ButtonGroup mediaTimeButtons = new ButtonGroup();
                JRadioButtonMenuItem showMediaTime = new JRadioButtonMenuItem("Show");
                showMediaTime.addActionListener(e -> {
                    Main.showUserTimes = true;
                });
                mediaTime.add(showMediaTime);
                JRadioButtonMenuItem hideMediaTime = new JRadioButtonMenuItem("Hide");
                hideMediaTime.addActionListener(e -> {
                    Main.showUserTimes = false;
                });
                hideMediaTime.setSelected(true);
                mediaTime.add(hideMediaTime);
                mediaTimeButtons.add(showMediaTime);
                mediaTimeButtons.add(hideMediaTime);
                controlPanel.add(mediaTime);
                controlPanel.add(new JSeparator());
                ButtonGroup showControlPanelButtons = new ButtonGroup();
                JRadioButtonMenuItem showControlPanel = new JRadioButtonMenuItem("Show");
                showControlPanel.addActionListener(e -> {
                    playbackPanel.getParent().add(GUI.controlPanel, rightControlPanel.isSelected() ? BorderLayout.EAST : BorderLayout.WEST);
                    playbackPanel.getParent().revalidate();
                    playbackPanel.getParent().repaint();
                });
                showControlPanel.setSelected(true);
                controlPanel.add(showControlPanel);
                JRadioButtonMenuItem hideControlPanel = new JRadioButtonMenuItem("Hide");
                hideControlPanel.addActionListener(e -> {
                    playbackPanel.getParent().remove(GUI.controlPanel);
                    playbackPanel.getParent().revalidate();
                    playbackPanel.getParent().repaint();
                });
                controlPanel.add(hideControlPanel);
                showControlPanelButtons.add(showControlPanel);
                showControlPanelButtons.add(hideControlPanel);
                ui.add(controlPanel);

            settings.add(ui);

            /**
             * Video settings
             */
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

            /**
             * Audio settings
             */
            JMenu audio = new JMenu("Audio Settings");
            JMenuItem audioEqualizer = new JMenuItem("Audio Equalizer");
            audioEqualizer.addActionListener(e -> {
                new AudioSettingsGUI();
            });
            audio.add(audioEqualizer);
            settings.add(audio);

        add(settings);
    }
}
