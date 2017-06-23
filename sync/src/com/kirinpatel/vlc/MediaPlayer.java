package com.kirinpatel.vlc;

import com.kirinpatel.gui.PlaybackPanel;
import com.kirinpatel.util.Debug;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultAdaptiveRuntimeFullScreenStrategy;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kirin Patel
 * @date 6/23/17
 */
public class MediaPlayer extends EmbeddedMediaPlayerComponent {

    private final MediaPlayerFactory factory;
    private final uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer;
    private final Canvas videoSurface;
    private final PlaybackPanel playbackPanel;
    private String mediaUrl = "";
    private long time = -1;
    private long mediaLength = -1;
    private boolean isPaused = false;

    public MediaPlayer(PlaybackPanel playbackPanel) {
        factory = getMediaPlayerFactory();
        mediaPlayer = getMediaPlayer();
        videoSurface = getVideoSurface();
        this.playbackPanel = playbackPanel;

        mediaPlayer.setStandardMediaOptions();
        mediaPlayer.setPlaySubItems(true);
        mediaPlayer.addMediaPlayerEventListener(new MediaEventListener());
    }

    private void initPlaybackPanelActions() {
        playbackPanel.mediaPosition.setMaximum(1000);
        if (playbackPanel.type == 0) {
            playbackPanel.pauseMedia.addActionListener(e -> {
                if (playbackPanel.pauseMedia.getText().equals("||")) mediaPlayer.pause();
                else mediaPlayer.play();
            });

            playbackPanel.mediaPosition.addChangeListener(e -> {
                int position = playbackPanel.mediaPosition.getValue();
                mediaPlayer.setTime(position * getMediaLength() / 1000);
            });
        }
    }

    public void setMediaURL(String mediaUrl) {
        if (!mediaUrl.isEmpty() && !mediaUrl.equals(this.mediaUrl)) {
            mediaPlayer.playMedia(mediaUrl);
            initPlaybackPanelActions();
        }
    }

    public void releaseMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void setVolume(int volume) {
        mediaPlayer.setVolume(volume);
    }

    public void seekTo(long time) {
        mediaPlayer.setTime(time);
    }

    public String getMediaURL() {
        return mediaUrl;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public long getMediaTime() {
        return time == -1? 0 : time;
    }

    public long getMediaLength() {
        return mediaLength == -1 ? 0 : mediaLength;
    }

    /**
     * Credit: https://github.com/caprica/vlcj-player/blob/master/src/main/java/uk/co/caprica/vlcjplayer/time/Time.java
     *
     * @param value Time
     * @return Time in displayable string format
     */
    private static String formatTime(long value) {
        value /= 1000;
        int hours = (int) value / 3600;
        int remainder = (int) value - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int seconds = remainder;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    class MediaEventListener implements MediaPlayerEventListener {

        @Override
        public void mediaChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, libvlc_media_t libvlc_media_t, String s) {
            mediaUrl = s;
        }

        @Override
        public void opening(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {

        }

        @Override
        public void buffering(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, float v) {
            if (v == 100.0) Debug.Log("Media buffered.", 1);
        }

        @Override
        public void playing(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            mediaLength = mediaPlayer.getLength();
            playbackPanel.pauseMedia.setText("||");
            isPaused = false;
        }

        @Override
        public void paused(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer) {
            playbackPanel.pauseMedia.setText(">");
            isPaused = true;
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
            playbackPanel.pauseMedia.setText("");
        }

        @Override
        public void timeChanged(uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer, long l) {
            time = l;
            playbackPanel.mediaPositionLabel.setText(formatTime(l) + " / " + formatTime(mediaLength));
            if (playbackPanel.type != 0) playbackPanel.mediaPosition.setValue((int) (time * 1000 / mediaLength));
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
            if (playbackPanel.type == 0) pause();
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
