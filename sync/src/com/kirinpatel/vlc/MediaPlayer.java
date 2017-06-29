package com.kirinpatel.vlc;

import com.kirinpatel.Main;
import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.gui.ServerGUI;
import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;
import com.kirinpatel.util.User;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
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

public class MediaPlayer extends JPanel {

    private final int WIDTH;
    private final int HEIGHT;
    private final PlaybackPanel playbackPanel;
    private final BufferedImage image;
    private final DirectMediaPlayer mediaPlayer;
    private BufferedImage scale;
    private boolean isPaused = true;
    private long time = -1;
    private long length = -1;
    private String mediaURL = "";
    private String filePath = "";
    private boolean isScrubbing = false;
    private boolean isFile = false;

    public MediaPlayer(PlaybackPanel playbackPanel) {
        new NativeDiscovery().discover();
        Debug.Log("Creating MediaPlayer...", 6);
        setBackground(Color.BLACK);
        setOpaque(true);

        WIDTH = (int) (1920 * Main.videoQuality);
        HEIGHT = (int) (1080 * Main.videoQuality);
        this.playbackPanel = playbackPanel;

        image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(WIDTH, HEIGHT);
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
        Debug.Log("MediaPlayer created.", 6);
    }

    private void initControls() {
        Debug.Log("Initializing media player controls...", 3);
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
            for (User client : Server.connectedClients) {
                client.setTime(0);
                ServerGUI.controlPanel.updateConnectedClientsTime(Server.connectedClients);
            }
        }

        isPaused = true;
        Debug.Log("Media player controls initialized.", 3);
    }

    public void play() {
        if (!mediaURL.isEmpty() && isPaused) {
            Debug.Log("Playing media.", 6);
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (!mediaURL.isEmpty() && !isPaused) {
            Debug.Log("Pausing media.", 6);
            mediaPlayer.pause();
        }
    }

    public void release() {
        Debug.Log("Releasing media player...", 6);
        mediaPlayer.stop();
        mediaPlayer.release();
        Debug.Log("Media player released.", 6);
    }

    public void setMediaURL(String mediaURL) {
        if (!mediaURL.isEmpty() && !mediaURL.equals(this.mediaURL)) {
            Debug.Log("Setting media url.", 6);
            isFile = false;
            mediaPlayer.prepareMedia(mediaURL);
            mediaPlayer.parseMedia();
            initControls();
        }
    }

    public void setMediaFile(String filePath, String mediaURL) {
        if (!filePath.isEmpty() && !mediaURL.isEmpty() && !filePath.equals(this.filePath) && !mediaURL.equals(this.mediaURL)) {
            Debug.Log("Setting media file.", 6);
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
        Debug.Log("Seeking media player (" + time + ").", 6);
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
        }
    }

    class MediaEventListener implements MediaPlayerEventListener {

        @Override
        public void mediaChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, libvlc_media_t libvlc_media_t, String s) {
            if (!isFile) mediaURL = s;
        }

        @Override
        public void opening(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            mediaPlayer.setVolume(playbackPanel.mediaVolume.getValue());
        }

        @Override
        public void buffering(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, float v) {
          
        }

        @Override
        public void playing(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            isPaused = false;
            length = mediaPlayer.getLength();
            PlaybackPanel.pauseMedia.setText("||");

            mediaPlayer.setMarqueeText("Playing");
            mediaPlayer.enableMarquee(true);
        }

        @Override
        public void paused(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            isPaused = true;
            length = mediaPlayer.getLength();
            PlaybackPanel.pauseMedia.setText(">");

            mediaPlayer.setMarqueeText("Paused");
            mediaPlayer.enableMarquee(true);
        }

        @Override
        public void stopped(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void forward(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void backward(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void finished(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            isPaused = true;
            PlaybackPanel.pauseMedia.setText(">");
            playbackPanel.mediaPosition.setValue(0);
        }

        @Override
        public void timeChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long l) {
            if (!isScrubbing) {
                time = l;
                playbackPanel.mediaPositionLabel.setText(formatTime(l) + " / " + formatTime(length));
                playbackPanel.mediaPosition.setValue((int) (time * 1000 / length));
            }
        }

        @Override
        public void positionChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, float v) {

        }

        @Override
        public void seekableChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void pausableChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void titleChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void snapshotTaken(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, String s) {

        }

        @Override
        public void lengthChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long l) {
            length = l;
        }

        @Override
        public void videoOutput(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void scrambledChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void elementaryStreamAdded(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i, int i1) {

        }

        @Override
        public void elementaryStreamDeleted(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i, int i1) {

        }

        @Override
        public void elementaryStreamSelected(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i, int i1) {

        }

        @Override
        public void corked(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, boolean b) {

        }

        @Override
        public void muted(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, boolean b) {

        }

        @Override
        public void volumeChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, float v) {

        }

        @Override
        public void audioDeviceChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, String s) {

        }

        @Override
        public void chapterChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void error(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void mediaMetaChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void mediaSubItemAdded(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, libvlc_media_t libvlc_media_t) {

        }

        @Override
        public void mediaDurationChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long l) {

        }

        @Override
        public void mediaParsedChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void mediaFreed(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void mediaStateChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void mediaSubItemTreeAdded(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, libvlc_media_t libvlc_media_t) {

        }

        @Override
        public void newMedia(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void subItemPlayed(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void subItemFinished(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void endOfSubItems(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }
    }
}
