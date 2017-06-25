package com.kirinpatel.gui;

import com.kirinpatel.Main;
import com.kirinpatel.vlc.MediaPlayer;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kirin Patel
 * @date 6/23/17
 */
public class PlaybackPanel extends JPanel {

    public static MediaPlayer mediaPlayer;
    public JButton pauseMedia;
    public JLabel mediaPositionLabel;
    public JSlider mediaPosition;
    public JSlider mediaVolume;
    public final int type;

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

    private void initControls() {
        Color foreground = Color.white;
        Color background = Color.black;
        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        controlPanel.setBackground(background);

        pauseMedia = new JButton("");
        pauseMedia.setBorder(null);
        pauseMedia.setEnabled(type == 0);
        pauseMedia.setBackground(background);
        pauseMedia.setForeground(foreground);
        controlPanel.add(pauseMedia);

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
