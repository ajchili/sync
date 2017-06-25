package com.kirinpatel.gui;

import com.kirinpatel.vlc.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlaybackPanel extends JPanel {

    public static MediaPlayer mediaPlayer;
    public JButton pauseMedia;
    public JLabel mediaPositionLabel;
    public JSlider mediaPosition;
    public JSlider mediaVolume;
    public final int type;
    private JFrame fullscreen;
    private boolean isFullscreen = false;

    public PlaybackPanel(int type) {
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
        fullscreen = new JFrame();
        fullscreen.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        fullscreen.setUndecorated(true);
        fullscreen.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 32 && type == 0) {
                   if(mediaPlayer.isPaused()) mediaPlayer.play();
                   else mediaPlayer.pause();
                } else if (e.getKeyCode() == 122 || e.getKeyCode() == 27) {
                    fullscreen.hide();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
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
        remove(mediaPlayer);
        fullscreen.add(mediaPlayer);
        fullscreen.setVisible(true);
    }

    public void closeFullscreen() {
        fullscreen.remove(mediaPlayer);
        fullscreen.dispose();
        add(mediaPlayer, BorderLayout.CENTER);
    }

    private void initControls() {
        Color foreground = Color.white;
        Color background = Color.black;
        JPanel controlPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        controlPanel.setBackground(background);

        pauseMedia = new JButton("");
        pauseMedia.setBorder(null);
        pauseMedia.setEnabled(type == 0);
        pauseMedia.setBackground(background);
        pauseMedia.setForeground(foreground);
        controlPanel.add(pauseMedia);

        JButton fullscreen = new JButton("Fullscreen");
        fullscreen.setBorder(null);
        fullscreen.setBackground(background);
        fullscreen.setForeground(foreground);
        fullscreen.addActionListener(e -> {
            if (!isFullscreen) initFullscreen();
        });
        controlPanel.add(fullscreen);


        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.setBackground(background);
        mediaPositionLabel = new JLabel("0:0:00 / 0:0:00");
        mediaPositionLabel.setOpaque(true);
        mediaPositionLabel.setBackground(background);
        mediaPositionLabel.setForeground(foreground);
        positionPanel.add(mediaPositionLabel, BorderLayout.EAST);
        mediaPosition = new JSlider(0, 0, 0);
        mediaPosition.setBackground(background);
        positionPanel.add(mediaPosition, BorderLayout.CENTER);
        controlPanel.add(positionPanel);

        JPanel volumePanel = new JPanel(new BorderLayout());
        JLabel mediaVolumeLabel = new JLabel("Volume: ");
        mediaVolumeLabel.setOpaque(true);
        mediaVolumeLabel.setBackground(background);
        mediaVolumeLabel.setForeground(foreground);
        volumePanel.add(mediaVolumeLabel, BorderLayout.WEST);
        mediaVolume = new JSlider(0, 100, 100 - type * 75);
        mediaVolume.addChangeListener(e -> mediaPlayer.setVolume(mediaVolume.getValue()));
        mediaVolume.setBackground(background);
        volumePanel.add(mediaVolume, BorderLayout.CENTER);
        controlPanel.add(volumePanel);

        add(controlPanel, BorderLayout.SOUTH);
    }
}
