package com.kirinpatel.gui;

import com.kirinpatel.Launcher;
import com.kirinpatel.net.Client;
import com.kirinpatel.net.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.*;

class MenuBar extends JMenuBar {

    MenuBar(PlaybackPanel playbackPanel) {
        super();

        /*
          Application section
         */
        JMenu sync = new JMenu("sync");

            /*
              Media section
             */
            JMenuItem file = new JMenuItem("Set Media");
            file.addActionListener(e -> {
                new MediaSelectorGUI();
            });
            if (playbackPanel.type == SERVER) {
                sync.add(file);
                sync.add(new JSeparator());
            }

            /*
              Share section
             */
            JMenuItem share = new JMenuItem("Share Server Address");
            share.addActionListener(e -> {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(playbackPanel.type == SERVER
                                        ? Server.ipAddress
                                        : Client.ipAddress),
                                null);
            });
            sync.add(share);
            sync.add(new JSeparator());

            /*
              Close section
             */
            JMenuItem close = new JMenuItem("Close sync");
            close.addActionListener(e -> {
                if (playbackPanel.type == SERVER) {
                    Server.stop();
                } else {
                    Client.stop();
                }
            });
            sync.add(close);

        add(sync);

        /*
          Settings section
         */
        JMenu settings = new JMenu("Settings");

            /*
             * Interface settings section
             */
            JMenu ui = new JMenu("Interface Settings");

                /*
                 * Fullscreen
                 */
                JMenuItem fullscreen = new JMenuItem("Launch Fullscreen");
                fullscreen.addActionListener(e -> {
                    playbackPanel.initFullscreen();
                });
                ui.add(fullscreen);
                ui.add(new JSeparator());

                /*
                 * Control Panel
                 */
                JMenu controlPanel = new JMenu("Control Panel");
                JPanel controlPanelSizePanel = new JPanel(new BorderLayout());
                JSlider controlPanelSizeSlider = new JSlider(200, 640, 300);
                controlPanelSizeSlider.setPaintTicks(true);
                controlPanelSizeSlider.setMajorTickSpacing(100);
                controlPanelSizeSlider.setMinorTickSpacing(50);
                controlPanelSizeSlider.setToolTipText("Size: " + controlPanelSizeSlider.getValue());
                controlPanelSizeSlider.addChangeListener(e -> {
                        ControlPanel.getInstance().width = controlPanelSizeSlider.getValue();
                        controlPanelSizeSlider.setToolTipText("Size: " + controlPanelSizeSlider.getValue());
                    });
                controlPanelSizePanel.add(new JLabel("Size: ", JLabel.LEFT), BorderLayout.WEST);
                controlPanelSizePanel.add(controlPanelSizeSlider, BorderLayout.CENTER);
                controlPanel.add(controlPanelSizePanel);
                JMenu controlPanelLocation = new JMenu("Location");
                ButtonGroup locationControlPanelButtons = new ButtonGroup();
                JRadioButtonMenuItem rightControlPanel = new JRadioButtonMenuItem("Display on Right");
                rightControlPanel.addActionListener(e -> {
                    if (playbackPanel.getParent().getComponents().length == 2) {
                        playbackPanel.getParent().remove(ControlPanel.getInstance());
                        playbackPanel.getParent().add(ControlPanel.getInstance(), BorderLayout.EAST);
                        playbackPanel.getParent().revalidate();
                        playbackPanel.getParent().repaint();
                    }
                });
                rightControlPanel.setSelected(true);
                controlPanelLocation.add(rightControlPanel);
                JRadioButtonMenuItem leftControlPanel = new JRadioButtonMenuItem("Display on Left");
                leftControlPanel.addActionListener(e -> {
                    if (playbackPanel.getParent().getComponents().length == 2) {
                        playbackPanel.getParent().remove(ControlPanel.getInstance());
                        playbackPanel.getParent().add(ControlPanel.getInstance(), BorderLayout.WEST);
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
                JSlider warningSlider = new JSlider(250, 4999, 1000);
                warningSlider.setPaintTicks(true);
                warningSlider.setMajorTickSpacing(100);
                warningSlider.setMinorTickSpacing(50);
                warningSlider.setToolTipText("Display desync warning after " + warningSlider.getValue() / 1000.0f + " seconds");
                warningSlider.addChangeListener(e -> {
                    Launcher.deSyncWarningTime = warningSlider.getValue();
                    warningSlider.setToolTipText("Display desync warning after " + warningSlider.getValue() / 1000.0f + " seconds");
                });
                warningTimePanel.add(warningSlider, BorderLayout.CENTER);
                mediaTimeIndicators.add(warningTimePanel);
                JPanel desyncTimePanel = new JPanel(new BorderLayout());
                desyncTimePanel.add(new JLabel("DeSync Time: ", JLabel.LEFT), BorderLayout.WEST);
                JSlider desyncSlider = new JSlider(5000, 10000, 5000);
                desyncSlider.setPaintTicks(true);
                desyncSlider.setMajorTickSpacing(500);
                desyncSlider.setMinorTickSpacing(250);
                desyncSlider.setToolTipText("Display desync after " + desyncSlider.getValue() / 1000.0f + " seconds");
                desyncSlider.addChangeListener(e -> {
                    Launcher.deSyncWarningTime = desyncSlider.getValue();
                    desyncSlider.setToolTipText("Display desync after " + desyncSlider.getValue() / 1000.0f + " seconds");
                });
                desyncTimePanel.add(desyncSlider, BorderLayout.CENTER);
                mediaTimeIndicators.add(desyncTimePanel);
                mediaTime.add(mediaTimeIndicators);
                mediaTime.add(new JSeparator());
                ButtonGroup mediaTimeButtons = new ButtonGroup();
                JRadioButtonMenuItem showMediaTime = new JRadioButtonMenuItem("Show");
                showMediaTime.addActionListener(e -> {
                    Launcher.showUserTimes = true;
                });
                mediaTime.add(showMediaTime);
                JRadioButtonMenuItem hideMediaTime = new JRadioButtonMenuItem("Hide");
                hideMediaTime.addActionListener(e -> {
                    Launcher.showUserTimes = false;
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
                    playbackPanel.getParent().add(ControlPanel.getInstance(), rightControlPanel.isSelected() ? BorderLayout.EAST : BorderLayout.WEST);
                    playbackPanel.getParent().revalidate();
                    playbackPanel.getParent().repaint();
                });
                showControlPanel.setSelected(true);
                controlPanel.add(showControlPanel);
                JRadioButtonMenuItem hideControlPanel = new JRadioButtonMenuItem("Hide");
                hideControlPanel.addActionListener(e -> {
                    playbackPanel.getParent().remove(ControlPanel.getInstance());
                    playbackPanel.getParent().revalidate();
                    playbackPanel.getParent().repaint();
                });
                controlPanel.add(hideControlPanel);
                showControlPanelButtons.add(showControlPanel);
                showControlPanelButtons.add(hideControlPanel);
                ui.add(controlPanel);

            settings.add(ui);

            /*
             * Video settings
             */
            JMenu video = new JMenu("Video Settings");
            JMenuItem videoSettings = new JMenuItem("Show Video Settings Window");
            video.add(videoSettings);
            // settings.add(video);

            /*
             * Audio settings
             */
            JMenu audio = new JMenu("Audio Settings");
            JMenuItem audioEqualizer = new JMenuItem("Audio Equalizer");
            audioEqualizer.addActionListener(e -> new AudioSettingsGUI());
            audio.add(audioEqualizer);
            settings.add(audio);

        add(settings);
    }
}
