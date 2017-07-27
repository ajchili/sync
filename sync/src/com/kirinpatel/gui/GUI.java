package com.kirinpatel.gui;

import com.kirinpatel.net.Server;
import com.kirinpatel.net.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 *
 */
public class GUI extends JFrame {

    private final int type;
    public static PlaybackPanel playbackPanel;
    public static ControlPanel controlPanel;

    /**
     * Primary constructor that will create the GUI of sync.
     *
     * @param type Type
     */
    public GUI(int type) {
        super(type == 0 ? "sync - Server" : "sync - Client (" + Client.ipAddress + ":8000)");
        this.type = type;

        setSize(new Dimension(940, 360));
        setMinimumSize(new Dimension(940, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        playbackPanel = new PlaybackPanel(type);
        add(playbackPanel, BorderLayout.CENTER);
        controlPanel = new ControlPanel(this, type);
        controlPanel.resizePanel(getHeight());
        add(controlPanel, BorderLayout.EAST);
        setJMenuBar(new MenuBar(playbackPanel));

        setVisible(type == 1);
    }

    /**
     * This inner class will handle all resizing and closing of the GUI class.
     */
    class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            controlPanel.resizePanel(getHeight());
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {
            PlaybackPanel.mediaPlayer.release();
            dispose();
            if (type == 0) Server.stop();
            else Client.stop();
        }
    }
}