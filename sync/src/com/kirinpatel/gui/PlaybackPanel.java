package com.kirinpatel.gui;

import com.kirinpatel.util.KeyDispatcher;
import com.kirinpatel.vlc.VLCJMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PlaybackPanel extends JPanel {

    public static VLCJMediaPlayer mediaPlayer;
    public static JButton pauseMedia;
    public final int type;
    public JLabel mediaPositionLabel;
    public JSlider mediaPosition;
    public JSlider mediaVolume;
    public boolean isFullscreen = false;
    private JPanel controlPanel;
    private JFrame fullscreen;
    private JPanel fullscreenPanel;
    private boolean showBar = false;
    private long lastClick = 0;
    private FullscreenListener fullscreenListener;

    PlaybackPanel(int type) {
        super(new BorderLayout());
        this.type = type;

        fullscreenListener = new FullscreenListener();

        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mediaPlayer = new VLCJMediaPlayer(this);
        add(mediaPlayer, BorderLayout.CENTER);
        mediaPlayer.addMouseListener(fullscreenListener);

        initControls();
    }

    public void initFullscreen() {
        removeAll();

        isFullscreen = true;
        fullscreen = new JFrame();
        fullscreen.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        fullscreen.setUndecorated(true);
        fullscreen.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getY() >= Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 50) {
                    showBar = true;
                    controlPanel.setVisible(true);
                    if (!mediaPlayer.getMediaURL().isEmpty() && !mediaPlayer.isPaused()) repaint();
                } else {
                    new Thread(() -> {
                        try {
                            showBar = false;
                            Thread.sleep(2000);

                            if (!showBar && isFullscreen) {
                                controlPanel.setVisible(false);
                                if (!mediaPlayer.getMediaURL().isEmpty() && !mediaPlayer.isPaused()) repaint();
                            }
                        } catch(InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }).start();
                }
            }
        });
        fullscreen.addMouseListener(new FullscreenListener());
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
        mediaPlayer.removeMouseListener(fullscreenListener);
        fullscreen.setVisible(true);
    }

    public void closeFullscreen() {
        isFullscreen = false;
        fullscreenPanel.removeAll();
        fullscreen.dispose();
        mediaPlayer.addMouseListener(fullscreenListener);
        add(mediaPlayer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        controlPanel.setVisible(true);
        if (!mediaPlayer.getMediaURL().isEmpty() && !mediaPlayer.isPaused()) {
            mediaPlayer.repaint();
            controlPanel.repaint();
            repaint();
        }
    }

    private void initControls() {
        Color foreground = Color.WHITE;
        Color background = Color.BLACK;
        controlPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        controlPanel.setBackground(background);

        pauseMedia = new JButton("");
        pauseMedia.setContentAreaFilled(false);
        pauseMedia.setInputMap(0, null);
        pauseMedia.setEnabled(type == 0);
        pauseMedia.setBackground(background);
        pauseMedia.setForeground(foreground);
        pauseMedia.setOpaque(true);
        pauseMedia.setBorderPainted(false);
        pauseMedia.setFocusable(false);
        controlPanel.add(pauseMedia);

        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.setBackground(background);
        mediaPositionLabel = new JLabel("0:0:00 / 0:0:00");
        mediaPositionLabel.setBackground(background);
        mediaPositionLabel.setForeground(foreground);
        mediaPositionLabel.setFocusable(false);
        positionPanel.add(mediaPositionLabel, BorderLayout.EAST);
        mediaPosition = new JSlider(0, 0, 0);
        mediaPosition.setOpaque(false);
        mediaPosition.setFocusable(false);
        positionPanel.add(mediaPosition, BorderLayout.CENTER);
        controlPanel.add(positionPanel);

        JPanel volumePanel = new JPanel(new BorderLayout());
        volumePanel.setBackground(background);
        JLabel mediaVolumeLabel = new JLabel("Volume: ");
        mediaVolumeLabel.setBackground(background);
        mediaVolumeLabel.setForeground(foreground);
        mediaVolumeLabel.setOpaque(true);
        mediaVolumeLabel.setFocusable(false);
        volumePanel.add(mediaVolumeLabel, BorderLayout.WEST);
        mediaVolume = new JSlider(0, 100, 25);
        mediaVolume.setOpaque(false);
        mediaVolume.setFocusable(false);
        mediaVolume.addChangeListener(e -> mediaPlayer.setVolume(mediaVolume.getValue()));
        volumePanel.add(mediaVolume, BorderLayout.CENTER);
        controlPanel.add(volumePanel);

        add(controlPanel, BorderLayout.SOUTH);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher(this));
    }

    class FullscreenListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (lastClick > System.currentTimeMillis() - 200) {
                if (isFullscreen) closeFullscreen();
                else initFullscreen();
            }

            lastClick = System.currentTimeMillis();
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!mediaPlayer.getMediaURL().isEmpty() && !mediaPlayer.isPaused()) {
                mediaPlayer.revalidate();
                mediaPlayer.repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!mediaPlayer.getMediaURL().isEmpty() && !mediaPlayer.isPaused()) {
                mediaPlayer.revalidate();
                mediaPlayer.repaint();
            }
        }
    }
}
