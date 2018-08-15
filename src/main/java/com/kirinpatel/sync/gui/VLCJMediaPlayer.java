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

public class VLCJMediaPlayer extends JPanel {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private final BufferedImage image;
    private BufferedImage scale;
    private Media media;
    private boolean isScrubbing = false;
    private boolean isFile = false;
    private final GUI gui;

    VLCJMediaPlayer(GUI gui) {
        this.gui = gui;
        setBackground(Color.BLACK);
        setOpaque(true);

        media  = new Media("");
        BufferFormatCallback bufferFormatCallback = (sourceWidth, sourceHeight) -> new RV32BufferFormat(WIDTH, HEIGHT);

        DirectMediaPlayerComponent mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new MediaRenderCallback();
            }
        };
        image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(WIDTH, HEIGHT);
        Equalizer equalizer = mediaPlayerComponent.getMediaPlayerFactory().newEqualizer();
        AudioSettingsGUI.setEqualizer(equalizer);
        AudioSettingsGUI.loadSettings();
        media.initPlayer(new MediaEventListener(), mediaPlayerComponent.getMediaPlayer());
        media.setEqualizer(equalizer);
    }

    private void initControls() {
        if (gui.playbackPanel.type == SERVER) {
            for (User client : Sync.connectedUsers) {
                client.getMedia().setCurrentTime(0);
                ControlPanel.getInstance().updateConnectedClients();
            }

            gui.playbackPanel.pauseMedia.addActionListener(e -> {
                try {
                    if (media.isPaused()) {
                        media.play();
                    } else {
                        media.pause();
                    }
                } catch (Error error) {
                    // Stop if Invalid memory access occurs
                    UIMessage.showErrorDialog(
                            new IOException("Unable to initialize media player.\n" +
                                    "Forcefully closing sync, please restart sync."),
                            "Media was unable to be set.");
                }
            });

            gui.playbackPanel.mediaPosition.addMouseListener(new MouseListener() {
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

            gui.playbackPanel.mediaPosition.addChangeListener(e -> {
                if (isScrubbing) {
                    int position = gui.playbackPanel.mediaPosition.getValue();
                    media.setTime(position * media.getLength() / 1000);
                }
            });
        }

        gui.playbackPanel.pauseMedia.setText(">");
        media.initControls();
        gui.playbackPanel.mediaPosition.setMaximum(1000);
        media.setPaused(true);
    }

    public void play() {
        try {
            media.play();
        } catch (Error e) {
            setMedia(new Media(""));
        }
    }

    public void pause() {
        try {
            media.pause();
        } catch (Error e) {
            // Reset media if Invalid memory access occurs
            setMedia(new Media(""));
        }
    }

    public void release() {
        try {
            media.stop();
            media.release();
        } catch (Error e) {
            // Stop if Invalid memory access occurs
            e.printStackTrace();
            UIMessage.showErrorDialog(
                    new IOException("Unable to release media player.\n Forcefully closing sync, please restart sync."),
                    "Media was unable to be set.");
        }
    }

    public void setMedia(Media media) {
        isFile = false;
        this.media = media;
        try {
            media.prepareMedia();
            media.parseMedia();
            initControls();
        } catch (Error e) {
            // Stop client if Invalid memory access occurs
            if (gui.playbackPanel.type == CLIENT) {
                UIMessage.showErrorDialog(new IOException("Unable to set media.\n" +
                                "Please restart sync and reconnect to the sync server."),
                        "Media was unable to be set.");
                Launcher.connectedUser.stop();
            }
        }
    }

    public void setMediaSource(Media media) {
        if (!media.getURL().equals(this.media.getURL())|| !media.getFilePath().equals(this.media.getFilePath())) {
            this.media.setURL(media.getURL());
            isFile = !media.getFilePath().equals("null");
            if (isFile) {
                this.media.setFilePath(Paths.get(media.getFilePath()));
            }
            this.media.prepareMedia();
            this.media.parseMedia();
            initControls();
        }
    }

    void setVolume(int volume) {
        media.setVolume(volume);
    }

    public void seekTo(long time) {
        try {
            if (media != null && !media.isPaused()) {
                media.setTime(time);
            }
        } catch (Error e) {
            // Reset media if Invalid memory access occurs
            setMedia(new Media(""));
        }
    }

    public void setRate(float rate) {
        if (!media.isPaused()) {
            try {
                media.setRate(rate);
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        media.setRate(1.0f);
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
        return media.isPaused();
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
                if (gui.playbackPanel.type == SERVER) {
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
            if (!isFile) media.setURL(s);
        }

        @Override
        public void opening(MediaPlayer mediaPlayer) {
            mediaPlayer.setVolume(gui.playbackPanel.mediaVolume.getValue());
        }

        @Override
        public void buffering(MediaPlayer mediaPlayer, float v) {

        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            media.setPaused(false);
            media.setLength(mediaPlayer.getLength());
            gui.playbackPanel.pauseMedia.setText("||");

            mediaPlayer.setMarqueeText("Playing");
            mediaPlayer.enableMarquee(true);
        }

        @Override
        public void paused(MediaPlayer mediaPlayer) {
            media.setPaused(true);
            media.setLength(mediaPlayer.getLength());
            gui.playbackPanel.pauseMedia.setText(">");

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
            gui.playbackPanel.mediaPosition.setValue(0);
            gui.playbackPanel.pauseMedia.setText(">");
            media.setPaused(true);
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long length) {
            if (!isScrubbing) {
                media.setCurrentTime(length);
                gui.playbackPanel.mediaPositionLabel.setText(
                        media.getFormattedTime() + " / " + media.getFormattedLength());
                gui.playbackPanel.mediaPosition.setValue(
                        (int) (media.getCurrentTime() * 1000 / media.getLength()));
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
        public void lengthChanged(MediaPlayer mediaPlayer, long length) {
            media.setLength(length);
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
            media.setCurrentTime(0);
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
