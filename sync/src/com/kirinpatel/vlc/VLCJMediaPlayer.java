package com.kirinpatel.vlc;

import com.kirinpatel.Main;
import com.kirinpatel.gui.AudioSettingsGUI;
import com.kirinpatel.gui.GUI;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.net.Client;
import com.kirinpatel.util.User;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
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

/**
 * Modified JPanel that will play media for the sync application.
 */
public class VLCJMediaPlayer extends JPanel {

    private final int WIDTH;
    private final int HEIGHT;
    private final PlaybackPanel playbackPanel;
    private final BufferedImage image;
    private final DirectMediaPlayer mediaPlayer;
    public static Equalizer equalizer;
    private BufferedImage scale;
    private boolean isPaused = true;
    private long time = -1;
    private long length = -1;
    private String mediaURL = "";
    private String filePath = "";
    private boolean isScrubbing = false;
    private boolean isFile = false;

    /**
     * Constructor that will return a MediaPlayer.
     *
     * @param playbackPanel Returns MediaPanel
     */
    public VLCJMediaPlayer(PlaybackPanel playbackPanel) {
        new NativeDiscovery().discover();
        setBackground(Color.BLACK);
        setOpaque(true);

        // Width and height values are not scalable because VLCJ does not support real-time scaling of mediaplayer.
        WIDTH = 1280; // (1280 * Main.videoQuality) / 100;
        HEIGHT = 720; //(720 * Main.videoQuality) / 100;
        this.playbackPanel = playbackPanel;


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
        equalizer = mediaPlayerComponent.getMediaPlayerFactory().newEqualizer();
        for (int i = 0; i < 10; i++) equalizer.setAmp(i, AudioSettingsGUI.loadSettings(i));
        mediaPlayer.setEqualizer(equalizer);
    }

    /**
     * Initialize media controls or reset them after media is changed.
     */
    private void initControls() {
        if (playbackPanel.type == 0 && playbackPanel.mediaPosition.getMaximum() != 1000) {
            PlaybackPanel.pauseMedia.addActionListener(e -> {
                if (isPaused) mediaPlayer.play();
                else mediaPlayer.pause();
            });

            playbackPanel.mediaPosition.addMouseListener(new MouseListener() {
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

            playbackPanel.mediaPosition.addChangeListener(e -> {
                if (isScrubbing) {
                    int position = playbackPanel.mediaPosition.getValue();
                    mediaPlayer.setTime(position * getMediaLength() / 1000);
                }
            });
        }

        PlaybackPanel.pauseMedia.setText(">");
        playbackPanel.mediaPosition.setMaximum(1000);
        mediaPlayer.setMarqueeSize(60);
        mediaPlayer.setMarqueeOpacity(200);
        mediaPlayer.setMarqueeColour(Color.white);
        mediaPlayer.setMarqueeTimeout(3500);
        mediaPlayer.setMarqueeLocation(50, 1000);

        if (playbackPanel.type == 0) {
            for (User client : Main.connectedUsers) {
                client.setTime(0);
                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
            }
        }

        isPaused = true;
    }

    public void play() {
        if (!mediaURL.isEmpty() && isPaused) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (!mediaURL.isEmpty() && !isPaused) {
            mediaPlayer.pause();
        }
    }

    public void release() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public void setMediaURL(String mediaURL) {
        if (!mediaURL.isEmpty() && !mediaURL.equals(this.mediaURL)) {
            isFile = false;
            mediaPlayer.prepareMedia(mediaURL.startsWith("_")
                    ? "http://" + Client.ipAddress + ":8080/" + mediaURL.substring(1)
                    : mediaURL);
            mediaPlayer.parseMedia();
            initControls();
        }
    }

    public void setMediaFile(String filePath, String mediaURL) {
        if (!filePath.isEmpty()
                && !mediaURL.isEmpty()
                && !filePath.equals(this.filePath)
                && !mediaURL.equals(this.mediaURL)) {
            isFile = true;
            this.mediaURL = mediaURL;
            mediaPlayer.prepareMedia(filePath);
            mediaPlayer.parseMedia();
            initControls();
        }
    }

    public void setVolume(int volume) {
        mediaPlayer.setVolume(volume);
    }

    public void seekTo(long time) {
        mediaPlayer.setTime(time);
    }

    public void setRate(float rate) {
        if (!isPaused) mediaPlayer.setRate(rate);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public long getMediaTime() {
        return time == -1 ? 0 : time;
    }

    public long getMediaLength() {
        return length == -1 ? 0 : length;
    }

    /**
     * Credit: https://github.com/caprica/vlcj-player/blob/master/src/main/java/uk/co/caprica/vlcjplayer/time/Time.java
     *
     * @param value Time
     * @return Time in displayable string format
     */
    public static String formatTime(long value) {
        value /= 1000;
        int hours = (int) value / 3600;
        int remainder = (int) value - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int seconds = remainder;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(scale, null, 0, 0);
    }

    class MediaRenderCallback extends RenderCallbackAdapter {

        public MediaRenderCallback() {
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
            if (playbackPanel.type == 0) {
                Main.connectedUsers.get(0).setTime(getMediaTime());
                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
            } else {
                Client.user.setTime(getMediaTime());
                GUI.controlPanel.updateConnectedClients(Main.connectedUsers);
            }
        }
    }

    class MediaEventListener implements MediaPlayerEventListener {

        @Override
        public void mediaChanged(MediaPlayer mediaPlayer,
                                 libvlc_media_t libvlc_media_t,
                                 String s) {
            if (!isFile) mediaURL = s;
        }

        @Override
        public void opening(MediaPlayer mediaPlayer) {
            mediaPlayer.setVolume(playbackPanel.mediaVolume.getValue());
        }

        @Override
        public void buffering(MediaPlayer mediaPlayer, float v) {
          
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            isPaused = false;
            length = mediaPlayer.getLength();
            PlaybackPanel.pauseMedia.setText("||");

            mediaPlayer.setMarqueeText("Playing");
            mediaPlayer.enableMarquee(true);
        }

        @Override
        public void paused(MediaPlayer mediaPlayer) {
            isPaused = true;
            length = mediaPlayer.getLength();
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
            playbackPanel.mediaPosition.setValue(0);
            PlaybackPanel.pauseMedia.setText(">");
            isPaused = true;
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long l) {
            if (!isScrubbing) {
                time = l;
                playbackPanel.mediaPositionLabel.setText(formatTime(l) + " / " + formatTime(length));
                playbackPanel.mediaPosition.setValue((int) (time * 1000 / length));
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
            length = l;
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
            time = 0;
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
