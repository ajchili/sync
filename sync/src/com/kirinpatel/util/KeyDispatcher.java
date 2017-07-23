package com.kirinpatel.util;

import com.kirinpatel.gui.PlaybackPanel;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyDispatcher implements KeyEventDispatcher {

    private final PlaybackPanel playbackPanel;

    public KeyDispatcher(PlaybackPanel playbackPanel) {
        this.playbackPanel = playbackPanel;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch(e.getID()) {
            case KeyEvent.KEY_PRESSED:
                switch(e.getKeyCode()) {
                    case 27:
                        if (playbackPanel.isFullscreen) {
                            playbackPanel.closeFullscreen();
                        }
                        break;
                    case 32:
                        if (playbackPanel.isFullscreen && playbackPanel.pauseMedia.isEnabled()) {
                            if (playbackPanel.mediaPlayer.isPaused()) playbackPanel.mediaPlayer.play();
                            else playbackPanel.mediaPlayer.pause();
                        }
                        break;
                    case 122:
                        if (playbackPanel.isFullscreen) playbackPanel.closeFullscreen();
                        else  playbackPanel.initFullscreen();
                        break;
                    default:
                        break;
                }
                break;
            case KeyEvent.KEY_RELEASED:
                break;
            default:
                break;
        }

        return false;
    }
}
