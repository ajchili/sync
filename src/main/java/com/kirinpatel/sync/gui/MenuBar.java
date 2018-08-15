package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.net.Client;
import com.kirinpatel.sync.net.Media;
import com.kirinpatel.sync.net.Server;
import com.kirinpatel.sync.util.UIMessage;
import org.jetbrains.annotations.NotNull;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

class MenuBar extends JMenuBar {

    private boolean canOpenMediaSelector = true;

    MenuBar(PlaybackPanel playbackPanel, GUI gui) {
        super();

        MediaSelector.Companion.addListener(new MediaSelectorListener() {

            @Override
            public void opened() {
                canOpenMediaSelector = false;
            }

            @Override
            public void mediaSelected(@NotNull Media media) {

            }

            @Override
            public void closed() {
                canOpenMediaSelector = true;
            }
        });

        JMenu menu = new JMenu("sync");

        JMenuItem file = new JMenuItem("Set Media");
        file.addActionListener(e -> {
            if (canOpenMediaSelector) {
                new MediaSelector();
            }
        });
        if (playbackPanel.type == SERVER) {
            menu.add(file);
            menu.add(new JSeparator());
        }

        JMenuItem share = new JMenuItem("Share Server Address");
        share.addActionListener(e -> Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(playbackPanel.type == SERVER
                                ? Server.ipAddress
                                : Client.ipAddress),
                        null));
        menu.add(share);
        menu.add(new JSeparator());

        JMenuItem feedback = new JMenuItem("Feedback");
        feedback.addActionListener(e -> {
            String url = "https://github.com/ajchili/sync/issues";
            try {
                Desktop.getDesktop().browse(java.net.URI.create(url));
            } catch (IOException exception) {
                UIMessage.showErrorDialog(exception, "Please go to \"" + url + "\" to provide feedback and " +
                        "report this issue.");
            }
        });
        menu.add(feedback);
        menu.add(new JSeparator());

        JMenuItem close = new JMenuItem("Close sync");
        close.addActionListener(e -> Launcher.connectedUser.stop());
        menu.add(close);
        add(menu);

        JMenu settings = new JMenu("Settings");

        JMenu ui = new JMenu("Interface Settings");

        JMenuItem fullscreen = new JMenuItem("Launch Fullscreen");
        fullscreen.addActionListener(e -> playbackPanel.initFullscreen());
        ui.add(fullscreen);

        JRadioButtonMenuItem darkMode = new JRadioButtonMenuItem("Dark Mode");
        darkMode.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                if (darkMode.isSelected()) {
                    UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
                } else {
                    UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
                }
            } catch(Exception e1) {
                UIMessage.showErrorDialog(e1, "Unable to set look and feel of sync");
            } finally {
                gui.repaint();
            }
        }));
        ui.add(darkMode);
        ui.add(new JSeparator());

        JMenu controlPanel = new JMenu("Control Panel");
        JPanel controlPanelSizePanel = new JPanel(new BorderLayout());
        JSlider controlPanelSizeSlider = new JSlider(200, 640, 300);
        controlPanelSizeSlider.setPaintTicks(true);
        controlPanelSizeSlider.setMajorTickSpacing(100);
        controlPanelSizeSlider.setMinorTickSpacing(50);
        controlPanelSizeSlider.setToolTipText("Size: " + controlPanelSizeSlider.getValue());
        controlPanelSizeSlider.addChangeListener(e -> {
                ControlPanel.getInstance().width = controlPanelSizeSlider.getValue();
                ControlPanel.getInstance().setWidth();
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
            ControlPanel.deSyncWarningTime = warningSlider.getValue();
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
            ControlPanel.deSyncTime = desyncSlider.getValue();
            desyncSlider.setToolTipText("Display desync after " + desyncSlider.getValue() / 1000.0f + " seconds");
        });
        desyncTimePanel.add(desyncSlider, BorderLayout.CENTER);
        mediaTimeIndicators.add(desyncTimePanel);
        mediaTime.add(mediaTimeIndicators);
        mediaTime.add(new JSeparator());
        ButtonGroup mediaTimeButtons = new ButtonGroup();
        JRadioButtonMenuItem showMediaTime = new JRadioButtonMenuItem("Show");
        showMediaTime.addActionListener(e -> ControlPanel.showUserTimes = true);
        mediaTime.add(showMediaTime);
        JRadioButtonMenuItem hideMediaTime = new JRadioButtonMenuItem("Hide");
        hideMediaTime.addActionListener(e -> ControlPanel.showUserTimes = false);
        hideMediaTime.setSelected(true);
        mediaTime.add(hideMediaTime);
        mediaTimeButtons.add(showMediaTime);
        mediaTimeButtons.add(hideMediaTime);
        controlPanel.add(mediaTime);
        controlPanel.add(new JSeparator());
        ButtonGroup showControlPanelButtons = new ButtonGroup();
        JRadioButtonMenuItem showControlPanel = new JRadioButtonMenuItem("Show");
        showControlPanel.addActionListener(e -> {
            playbackPanel.getParent().add(ControlPanel.getInstance(),
                    rightControlPanel.isSelected() ? BorderLayout.EAST : BorderLayout.WEST);
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

        // TODO: Implement "Video Settings"
        JMenu video = new JMenu("Video Settings");
        JMenuItem videoSettings = new JMenuItem("Show Video Settings Window");
        video.add(videoSettings);
        // settings.add(video);

        JMenu audio = new JMenu("Audio Settings");
        JMenuItem audioEqualizer = new JMenuItem("Audio Equalizer");
        audioEqualizer.addActionListener(e -> new AudioSettingsGUI());
        audio.add(audioEqualizer);
        settings.add(audio);

        add(settings);
    }
}
