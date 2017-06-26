package com.kirinpatel.gui;

import com.kirinpatel.util.Debug;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyDispatcher implements KeyEventDispatcher {

    private final PlaybackPanel playbackPanel;

    public KeyDispatcher(PlaybackPanel playbackPanel) {
        Debug.Log("Creating KeyDispatcher...", 1);
        this.playbackPanel = playbackPanel;
        Debug.Log("KeyDispatcher created.", 1);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch(e.getID()) {
            case KeyEvent.KEY_PRESSED:
                switch (e.getKeyCode()) {
                    case 27:
                        if (playbackPanel.isFullscreen) {
                            Debug.Log("Closing fullscreen...", 3);
                            playbackPanel.closeFullscreen();
                        }
                        break;
                    case 32:
                        if (playbackPanel.isFullscreen) {
                            if(playbackPanel.mediaPlayer.isPaused()) playbackPanel.mediaPlayer.play();
                            else playbackPanel.mediaPlayer.pause();
                        }
                        break;
                    case 122:
                        if (playbackPanel.isFullscreen) {
                            Debug.Log("Closing fullscreen...", 3);
                            playbackPanel.closeFullscreen();
                        } else {
                            Debug.Log("Opening fullscreen...", 3);
                            playbackPanel.initFullscreen();
                        }
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
