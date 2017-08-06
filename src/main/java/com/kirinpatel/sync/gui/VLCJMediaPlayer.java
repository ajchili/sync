package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.net.Client;
import com.kirinpatel.sync.net.Media;
import com.kirinpatel.sync.net.Server;
import com.kirinpatel.sync.net.User;
import com.kirinpatel.sync.Sync;
import com.kirinpatel.sync.util.UIMessage;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.Equalizer;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

/**
 * Modified JPanel that will play media for the sync application.
 */
public class VLCJMediaPlayer extends JPanel {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private final BufferedImage image;
    private final DirectMediaPlayer mediaPlayer;
    private BufferedImage scale;
    private static Media media;
    private boolean isScrubbing = false;
    private boolean isFile = false;

    /**
     * Constructor that will return a MediaPlayer.
     */
    VLCJMediaPlayer() {
        setBackground(Color.BLACK);
        setOpaque(true);

        media  = new Media("");

        image = GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .createCompatibleImage(WIDTH, HEIGHT);
        BufferFormatCallback bufferFormatCallback = (sourceWidth, sourceHeight) -> new RV32BufferFormat(WIDTH, HEIGHT);
        DirectMediaPlayerComponent mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new MediaRenderCallback();
            }
        };
        mediaPlayer = mediaPlayerComponent.getMediaPlayer();
        mediaPlayer.setStandardMediaOptions();
        mediaPlayer.setPlaySubItems(true);
        mediaPlayer.addMediaPlayerEventListener(new MediaEventListener());
        Equalizer equalizer = mediaPlayerComponent.getMediaPlayerFactory().newEqualizer();
        AudioSettingsGUI.setEqualizer(equalizer);
        AudioSettingsGUI.loadSettings();
        mediaPlayer.setEqualizer(equalizer);
    }

    /**
     * Initialize media controls or reset them after media is changed.
     */
    private void initControls() {
        if (GUI.playbackPanel.type == SERVER) {
            for (User client : Sync.connectedUsers) {
                client.getMedia().setCurrentTime(0);
                ControlPanel.getInstance().updateConnectedClients();
            }

            PlaybackPanel.pauseMedia.addActionListener(e -> {
                try {
                    if (media.isPaused()) {
                        mediaPlayer.play();
                    } else {
                        mediaPlayer.pause();
                    }
                } catch (Error error) {
                    // Stop if Invalid memory access occurs
                    if (GUI.playbackPanel.type == SERVER) {
                        new UIMessage(Server.gui).showErrorDialogAndExit(new IOException("Unable to initialize media player.\n" +
                                        "Forcefully closing sync, please restart sync."),
                                "Media was unable to be set.");
                    } else {
                        new UIMessage(Client.gui).showErrorDialogAndExit(new IOException("Unable to initialize media player.\n" +
                                        "Forcefully closing sync, please restart sync."),
                                "Media was unable to be set.");
                    }
                }
            });

            GUI.playbackPanel.mediaPosition.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isScrubbing = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isScrubbing = false;
                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            GUI.playbackPanel.mediaPosition.addChangeListener(e -> {
                if (isScrubbing) {
                    int position = GUI.playbackPanel.mediaPosition.getValue();
                    mediaPlayer.setTime(position * media.getLength() / 1000);
                }
            });
        }

        PlaybackPanel.pauseMedia.setText(">");
        GUI.playbackPanel.mediaPosition.setMaximum(1000);
        mediaPlayer.setMarqueeSize(60);
        mediaPlayer.setMarqueeOpacity(200);
        mediaPlayer.setMarqueeColour(Color.white);
        mediaPlayer.setMarqueeTimeout(3500);
        mediaPlayer.setMarqueeLocation(50, 1000);

        media.setPaused(true);
    }

    public void play() {
        try {
            if (!media.getURL().isEmpty() && media.isPaused()) {
                VLCJMediaPlayer.media.setPaused(false);
                mediaPlayer.play();
            }
        } catch (Error e) {
            // Reset media if Invalid memory access occurs
            setMedia(new Media(""));
        }
    }

    public void pause() {
        try {
            if (!media.getURL().isEmpty() && !media.isPaused()) {
                VLCJMediaPlayer.media.setPaused(true);
                mediaPlayer.pause();
            }
        } catch (Error e) {
            // Reset media if Invalid memory access occurs
            setMedia(new Media(""));
        }
    }

    public void release() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Error e) {
            // Stop if Invalid memory access occurs
            if (GUI.playbackPanel.type == SERVER) {
                new UIMessage(Server.gui).showErrorDialogAndExit(new IOException("Unable to release media player.\n" +
                                "Forcefully closing sync, please restart sync."),
                        "Media was unable to be set.");
            } else {
                new UIMessage(Client.gui).showErrorDialogAndExit(new IOException("Unable to release media player.\n" +
                                "Forcefully closing sync, please restart sync."),
                        "Media was unable to be set.");
            }
        }
    }

    public void setMedia(Media media) {
        isFile = false;
        VLCJMediaPlayer.media = media;
        try {
            mediaPlayer.prepareMedia(VLCJMediaPlayer.media.getURL());
            mediaPlayer.parseMedia();
            initControls();
        } catch (Error e) {
            // Stop client if Invalid memory access occurs
            if (GUI.playbackPanel.type == CLIENT) {
                new UIMessage(Client.gui).showErrorDialogAndExit(new IOException("Unable to set media.\n" +
                                "Please restart sync and reconnect to the sync server."),
                        "Media was unable to be set.");
                Client.stop();
            }
        }
    }

    void setMediaSource(Media media) {
        if (!media.getURL().equals(VLCJMediaPlayer.media.getURL())|| !media.getFilePath().equals(VLCJMediaPlayer.media.getFilePath())) {
            VLCJMediaPlayer.media.setURL(media.getURL());
            isFile = !media.getFilePath().equals("null");
            if (isFile) {
                VLCJMediaPlayer.media.setFilePath(Paths.get(media.getFilePath()));
                mediaPlayer.prepareMedia(VLCJMediaPlayer.media.getFilePath());
            } else {
                mediaPlayer.prepareMedia(VLCJMediaPlayer.media.getURL());
            }
            mediaPlayer.parseMedia();
            initControls();
        }
    }

    void setVolume(int volume) {
        mediaPlayer.setVolume(volume);
    }

    public void seekTo(long time) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                VLCJMediaPlayer.media.setCurrentTime(time);
                mediaPlayer.setTime(time);
            }
        } catch (Error e) {
            // Reset media if Invalid memory access occurs
            setMedia(new Media(""));
        }
    }

    public void setRate(float rate) {
        if (!media.isPaused()) {
            try {
                VLCJMediaPlayer.media.setRate(rate);
                mediaPlayer.setRate(VLCJMediaPlayer.media.getRate());
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        VLCJMediaPlayer.media.setRate(1.0f);
                        mediaPlayer.setRate(VLCJMediaPlayer.media.getRate());
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } catch (Error e) {
                // Reset media if Invalid memory access occurs
                setMedia(new Media(""));
            }
        }
    }

    public Media getMedia() {
        return media;
    }

    public boolean isPaused() {
        return !mediaPlayer.isPlaying();
    }

    /**
     * Credit: https://github.com/caprica/vlcj-player/blob/master/src/main/java/uk/co/caprica/vlcjplayer/time/Time.java
     *
     * @param value Time
     * @return Time in displayable string format
     */
    static String formatTime(long value) {
        value /= 1000;
        int hours = (int) value / 3600;
        int remainder = (int) value - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int seconds = remainder;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(scale, null, 0, 0);
    }

    class MediaRenderCallback extends RenderCallbackAdapter {

        MediaRenderCallback() {
            super(new int[WIDTH * HEIGHT]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer directMediaPlayer, int[] buffer) {
            float xScale = (float) getWidth() / WIDTH;
            float yScale = (float) getHeight() / HEIGHT;
            image.setRGB(0, 0, WIDTH, HEIGHT, buffer, 0, WIDTH);
            BufferedImage after = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(xScale, yScale);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            scale = scaleOp.filter(image, after);
            repaint();
            if (Sync.connectedUsers.size() > 0) {
                if (GUI.playbackPanel.type == SERVER) {
                    Sync.connectedUsers.get(0).getMedia().setCurrentTime(media.getCurrentTime());
                } else {
                    Client.user.getMedia().setCurrentTime(media.getCurrentTime());
                }
                ControlPanel.getInstance().updateConnectedClients();
            }
        }
    }

    class MediaEventListener implements MediaPlayerEventListener {

        @Override
        public void mediaChanged(MediaPlayer mediaPlayer,
                                 libvlc_media_t libvlc_media_t,
                                 String s) {
            if (!isFile) VLCJMediaPlayer.media.setURL(s);
        }

        @Override
        public void opening(MediaPlayer mediaPlayer) {
            mediaPlayer.setVolume(GUI.playbackPanel.mediaVolume.getValue());
        }

        @Override
        public void buffering(MediaPlayer mediaPlayer, float v) {
          
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            VLCJMediaPlayer.media.setPaused(false);
            VLCJMediaPlayer.media.setLength(mediaPlayer.getLength());
            PlaybackPanel.pauseMedia.setText("||");

            mediaPlayer.setMarqueeText("Playing");
            mediaPlayer.enableMarquee(true);
        }

        @Override
        public void paused(MediaPlayer mediaPlayer) {
            VLCJMediaPlayer.media.setPaused(true);
            VLCJMediaPlayer.media.setLength(mediaPlayer.getLength());
            PlaybackPanel.pauseMedia.setText(">");

            mediaPlayer.setMarqueeText("Paused");
            mediaPlayer.enableMarquee(true);
        }

        @Override
        public void stopped(MediaPlayer mediaPlayer) {

        }

        @Override
        public void forward(MediaPlayer mediaPlayer) {

        }

        @Override
        public void backward(MediaPlayer mediaPlayer) {

        }

        @Override
        public void finished(MediaPlayer mediaPlayer) {
            GUI.playbackPanel.mediaPosition.setValue(0);
            PlaybackPanel.pauseMedia.setText(">");
            VLCJMediaPlayer.media.setPaused(true);
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long l) {
            if (!isScrubbing) {
                VLCJMediaPlayer.media.setCurrentTime(l);
                GUI.playbackPanel.mediaPositionLabel.setText(formatTime(l) + " / " + formatTime(VLCJMediaPlayer.media.getLength()));
                GUI.playbackPanel.mediaPosition.setValue((int) (VLCJMediaPlayer.media.getCurrentTime() * 1000 / VLCJMediaPlayer.media.getLength()));
            }
        }

        @Override
        public void positionChanged(MediaPlayer mediaPlayer, float v) {

        }

        @Override
        public void seekableChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void pausableChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void titleChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void snapshotTaken(MediaPlayer mediaPlayer, String s) {

        }

        @Override
        public void lengthChanged(MediaPlayer mediaPlayer, long l) {
            VLCJMediaPlayer.media.setLength(l);
        }

        @Override
        public void videoOutput(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void scrambledChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void elementaryStreamAdded(MediaPlayer mediaPlayer, int i, int i1) {

        }

        @Override
        public void elementaryStreamDeleted(MediaPlayer mediaPlayer, int i, int i1) {

        }

        @Override
        public void elementaryStreamSelected(MediaPlayer mediaPlayer, int i, int i1) {

        }

        @Override
        public void corked(MediaPlayer mediaPlayer, boolean b) {

        }

        @Override
        public void muted(MediaPlayer mediaPlayer, boolean b) {

        }

        @Override
        public void volumeChanged(MediaPlayer mediaPlayer, float v) {

        }

        @Override
        public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {

        }

        @Override
        public void chapterChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void error(MediaPlayer mediaPlayer) {

        }

        @Override
        public void mediaMetaChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t libvlc_media_t) {

        }

        @Override
        public void mediaDurationChanged(MediaPlayer mediaPlayer, long l) {

        }

        @Override
        public void mediaParsedChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void mediaFreed(MediaPlayer mediaPlayer) {

        }

        @Override
        public void mediaStateChanged(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void mediaSubItemTreeAdded(MediaPlayer mediaPlayer, libvlc_media_t libvlc_media_t) {

        }

        @Override
        public void newMedia(MediaPlayer mediaPlayer) {
            VLCJMediaPlayer.media.setCurrentTime(0);
        }

        @Override
        public void subItemPlayed(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void subItemFinished(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void endOfSubItems(MediaPlayer mediaPlayer) {

        }
    }
}
