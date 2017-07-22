package com.kirinpatel.gui;

import com.kirinpatel.net.Server;
import com.kirinpatel.net.Client;
import com.kirinpatel.util.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class GUI extends JFrame {

    private final int type;
    public static PlaybackPanel playbackPanel;
    public static ControlPanel controlPanel;

    public GUI(int type) {
        super(type == 0 ? "sync - Server" : "sync - Client (" + Client.ipAddress + ":8000)");
        this.type = type;

        Debug.Log("Starting server gui...", 3);

        setSize(new Dimension(840, 360));
        setMinimumSize(new Dimension(840, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        playbackPanel = new PlaybackPanel(type);
        add(playbackPanel, BorderLayout.CENTER);
        controlPanel = new ControlPanel(type);
        controlPanel.resizePanel(getHeight());
        add(controlPanel, BorderLayout.EAST);
        setJMenuBar(new MenuBar(playbackPanel));

        setVisible(true);

        Debug.Log(type == 0 ? "Server gui displayed." : "Client gui displayed.", 3);
    }

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
            Debug.Log(type == 0 ? "Closing server gui..." : "Closing client gui...", 3);
            PlaybackPanel.mediaPlayer.release();
            dispose();
            Debug.Log(type == 0 ? "Server gui closed." : "Client gui closed.", 3);
            if (type == 0) Server.stop();
            else Client.stop();
        }
    }
}