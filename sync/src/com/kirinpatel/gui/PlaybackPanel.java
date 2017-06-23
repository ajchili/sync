package com.kirinpatel.gui;

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
    public final int type;

    public PlaybackPanel(int type) {
        super(new BorderLayout());

        this.type = type;

        new NativeDiscovery().discover();
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer(this);
        add(mediaPlayer, BorderLayout.CENTER);

        initControls();
    }

    private void initControls() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        controlPanel.setBackground(Color.BLACK);

        pauseMedia = new JButton("");
        pauseMedia.setBorder(null);
        pauseMedia.setEnabled(type == 0);
        pauseMedia.setBackground(Color.BLACK);
        pauseMedia.setForeground(Color.WHITE);
        controlPanel.add(pauseMedia);

        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.setBackground(Color.BLACK);
        mediaPositionLabel = new JLabel("0:0:00 / 0:0:00");
        mediaPositionLabel.setOpaque(true);
        mediaPositionLabel.setBackground(Color.BLACK);
        mediaPositionLabel.setForeground(Color.WHITE);
        positionPanel.add(mediaPositionLabel, BorderLayout.EAST);
        mediaPosition = new JSlider(0, 0, 0);
        mediaPosition.setBackground(Color.BLACK);
        positionPanel.add(mediaPosition, BorderLayout.CENTER);
        controlPanel.add(positionPanel);

        JPanel volumePanel = new JPanel(new BorderLayout());
        JLabel mediaVolumeLabel = new JLabel("Volume: ");
        mediaVolumeLabel.setOpaque(true);
        mediaVolumeLabel.setBackground(Color.BLACK);
        mediaVolumeLabel.setForeground(Color.WHITE);
        volumePanel.add(mediaVolumeLabel, BorderLayout.WEST);
        JSlider mediaVolume = new JSlider(0, 100, 100 - type * 75);
        mediaVolume.addChangeListener(e -> {
            mediaPlayer.setVolume(mediaVolume.getValue());
        });
        mediaVolume.setBackground(Color.BLACK);
        volumePanel.add(mediaVolume, BorderLayout.CENTER);
        controlPanel.add(volumePanel);

        add(controlPanel, BorderLayout.SOUTH);
    }
}
