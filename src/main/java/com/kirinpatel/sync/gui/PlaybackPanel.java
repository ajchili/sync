package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.net.Media;
import com.kirinpatel.sync.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

public class PlaybackPanel extends JPanel {

    private VLCJMediaPlayer mediaPlayer;
    JButton pauseMedia;
    final PANEL_TYPE type;
    JLabel mediaPositionLabel;
    JSlider mediaPosition;
    JSlider mediaVolume;
    private boolean isFullscreen = false;
    private JPanel controlPanel;
    private JFrame fullscreen;
    private JPanel fullscreenPanel;
    private boolean showBar = false;
    private long lastClick = 0;
    private FullscreenListener fullscreenListener;

    public enum PANEL_TYPE {
        SERVER(0),
        CLIENT(1);

        private int panelType;

        PANEL_TYPE(int panelType) {
            this.panelType = panelType;
        }

        public int getPanelType() {
            return panelType;
        }
    }

    PlaybackPanel(PANEL_TYPE type, GUI gui) {
        super(new BorderLayout());
        this.type = type;
        setBackground(Theme.DARK_MODE_BACKGROUND);

        initMediaPlayer(gui);
    }

    private void initMediaPlayer(GUI gui) {
        fullscreenListener = new FullscreenListener();

        mediaPlayer = new VLCJMediaPlayer(gui);
        add(mediaPlayer, BorderLayout.CENTER);
        mediaPlayer.addMouseListener(fullscreenListener);

        initControls();
    }

    void initFullscreen() {
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
                    if (!mediaPlayer.getMedia().getURL().isEmpty() && !mediaPlayer.getMedia().isPaused()) {
                        repaint();
                    }
                } else {
                    new Thread(() -> {
                        try {
                            showBar = false;
                            Thread.sleep(2000);

                            if (!showBar && isFullscreen) {
                                controlPanel.setVisible(false);
                                if (!mediaPlayer.getMedia().getURL().isEmpty() && !mediaPlayer.getMedia().isPaused()) {
                                    repaint();
                                }
                            }
                        } catch(InterruptedException e1) {
                            Thread.currentThread().interrupt();
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

    private void closeFullscreen() {
        isFullscreen = false;
        fullscreenPanel.removeAll();
        fullscreen.dispose();
        mediaPlayer.addMouseListener(fullscreenListener);
        add(mediaPlayer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        controlPanel.setVisible(true);
        if (!mediaPlayer.getMedia().getURL().isEmpty() && !mediaPlayer.getMedia().isPaused()) {
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
        pauseMedia.setEnabled(type == SERVER);
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

    public Media getMedia() {
        return mediaPlayer.getMedia();
    }

    public VLCJMediaPlayer getMediaPlayer() {
        return mediaPlayer;
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
            if (!mediaPlayer.getMedia().getURL().isEmpty() && !mediaPlayer.getMedia().isPaused()) {
                mediaPlayer.revalidate();
                mediaPlayer.repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!mediaPlayer.getMedia().getURL().isEmpty() && !mediaPlayer.getMedia().isPaused()) {
                mediaPlayer.revalidate();
                mediaPlayer.repaint();
            }
        }
    }


    class KeyDispatcher implements KeyEventDispatcher {

        private final PlaybackPanel playbackPanel;

        KeyDispatcher(PlaybackPanel playbackPanel) {
            this.playbackPanel = playbackPanel;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            switch(e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    switch(e.getKeyCode()) {
                        // ESC
                        case 27:
                            if (playbackPanel.isFullscreen) {
                                playbackPanel.closeFullscreen();
                            }
                            break;
                        // Space bar
                        case 32:
                            if (playbackPanel.isFullscreen) {
                                if (mediaPlayer.isPaused()) {
                                    mediaPlayer.play();
                                } else {
                                    mediaPlayer.pause();
                                }
                            }
                            break;
                        // F11
                        case 122:
                            if (playbackPanel.isFullscreen) {
                                playbackPanel.closeFullscreen();
                            } else  {
                                playbackPanel.initFullscreen();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }

            return false;
        }
    }
}
