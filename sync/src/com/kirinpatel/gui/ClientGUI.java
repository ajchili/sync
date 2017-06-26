package com.kirinpatel.gui;

import com.kirinpatel.net.Client;
import com.kirinpatel.util.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ClientGUI extends JFrame {

    public static PlaybackPanel playbackPanel;
    public static ControlPanel controlPanel;

    public ClientGUI() {
        super("sync - Client (" + Client.ipAddress + ":8000)");

        Debug.Log("Starting client gui...", 3);

        setSize(new Dimension(800, 360));
        setMinimumSize(new Dimension(800, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        playbackPanel = new PlaybackPanel(1);
        add(playbackPanel, BorderLayout.CENTER);
        controlPanel = new ControlPanel(1);
        controlPanel.resizePanel(getWidth(), getHeight());
        add(controlPanel, BorderLayout.EAST);

        setVisible(true);

        Debug.Log("Client gui displayed.", 3);
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
            Debug.Log("Closing client gui...", 3);
            PlaybackPanel.mediaPlayer.release();
            dispose();
            Debug.Log("Client gui closed.", 3);
            Client.stop();
        }
    }
}
