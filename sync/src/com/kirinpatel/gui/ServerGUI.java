package com.kirinpatel.gui;

import com.kirinpatel.net.Server;
import com.kirinpatel.util.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author Kirin Patel
 * @date 6/16/17
 */
public class ServerGUI extends JFrame {

    public static PlaybackPanel playbackPanel;
    public static ControlPanel controlPanel;

    public ServerGUI() {
        super("sync - Server");

        Debug.Log("Starting server gui...", 3);

        setSize(new Dimension(800, 360));
        setMinimumSize(new Dimension(800, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        playbackPanel = new PlaybackPanel(0);
        add(playbackPanel, BorderLayout.CENTER);
        controlPanel = new ControlPanel(0);
        controlPanel.resizePanel(getWidth(), getHeight());
        add(controlPanel, BorderLayout.EAST);

        setVisible(true);

        Debug.Log("Server gui displayed.", 3);
    }

    class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            controlPanel.resizePanel(getWidth(), getHeight());
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {
            Debug.Log("Closing server gui...", 3);
            PlaybackPanel.mediaPlayer.release();
            dispose();
            Debug.Log("Server gui closed.", 3);
            Server.stop();
        }
    }
}
