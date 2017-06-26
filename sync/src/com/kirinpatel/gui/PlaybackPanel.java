package com.kirinpatel.gui;

import com.kirinpatel.vlc.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class PlaybackPanel extends JPanel {

    public static MediaPlayer mediaPlayer;
    private JPanel controlPanel;
    public JButton pauseMedia;
    public JLabel mediaPositionLabel;
    public JSlider mediaPosition;
    public JSlider mediaVolume;
    public final int type;
    private JFrame fullscreen;
    private JPanel fullscreenPanel;
    public boolean isFullscreen = false;

    PlaybackPanel(int type) {
        super(new BorderLayout());

        this.type = type;

        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer(this);
        add(mediaPlayer, BorderLayout.CENTER);

        initControls();
    }

    public void initFullscreen() {
        removeAll();

        isFullscreen = true;
        fullscreen = new JFrame();
        fullscreen.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        fullscreen.setUndecorated(true);
        fullscreen.addComponentListener(new ComponentListener() {
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
                closeFullscreen();
            }
        });
        fullscreenPanel = new JPanel(new BorderLayout());
        fullscreenPanel.add(mediaPlayer, BorderLayout.CENTER);
        fullscreenPanel.add(controlPanel, BorderLayout.SOUTH);
        fullscreen.add(fullscreenPanel);
        fullscreen.setVisible(true);
    }

    public void closeFullscreen() {
        isFullscreen = false;
        fullscreenPanel.removeAll();
        fullscreen.dispose();
        add(mediaPlayer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        mediaPlayer.repaint();
        controlPanel.repaint();
        repaint();
    }

    private void initControls() {
        Color foreground = Color.white;
        Color background = Color.black;
        controlPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        controlPanel.setBackground(background);

        pauseMedia = new JButton("");
        pauseMedia.setBorder(null);
        pauseMedia.setInputMap(0, null);
        pauseMedia.setEnabled(type == 0);
        pauseMedia.setBackground(background);
        pauseMedia.setForeground(foreground);
        pauseMedia.setFocusable(false);
        controlPanel.add(pauseMedia);

        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.setBackground(background);
        mediaPositionLabel = new JLabel("0:0:00 / 0:0:00");
        mediaPositionLabel.setOpaque(true);
        mediaPositionLabel.setBackground(background);
        mediaPositionLabel.setForeground(foreground);
        mediaPositionLabel.setFocusable(false);
        positionPanel.add(mediaPositionLabel, BorderLayout.EAST);
        mediaPosition = new JSlider(0, 0, 0);
        mediaPosition.setBackground(background);
        mediaPosition.setFocusable(false);
        positionPanel.add(mediaPosition, BorderLayout.CENTER);
        controlPanel.add(positionPanel);

        JPanel volumePanel = new JPanel(new BorderLayout());
        JLabel mediaVolumeLabel = new JLabel("Volume: ");
        mediaVolumeLabel.setOpaque(true);
        mediaVolumeLabel.setBackground(background);
        mediaVolumeLabel.setForeground(foreground);
        mediaVolumeLabel.setFocusable(false);
        volumePanel.add(mediaVolumeLabel, BorderLayout.WEST);
        mediaVolume = new JSlider(0, 100, 100 - type * 75);
        mediaVolume.addChangeListener(e -> mediaPlayer.setVolume(mediaVolume.getValue()));
        mediaVolume.setBackground(background);
        mediaVolume.setFocusable(false);
        volumePanel.add(mediaVolume, BorderLayout.CENTER);
        controlPanel.add(volumePanel);

        add(controlPanel, BorderLayout.SOUTH);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher(this));
    }
}
