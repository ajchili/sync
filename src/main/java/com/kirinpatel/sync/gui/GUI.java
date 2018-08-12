package com.kirinpatel.sync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

public class GUI extends JFrame {

    private final PlaybackPanel.PANEL_TYPE type;
    public PlaybackPanel playbackPanel;

    public GUI(PlaybackPanel.PANEL_TYPE type) {
        super(type == SERVER ? "sync - Server" : "sync - Client");
        this.type = type;

        setSize(new Dimension(940, 360));
        setMinimumSize(new Dimension(940, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setDefaultLookAndFeelDecorated(true);
        addComponentListener(new ResizeListener());

        playbackPanel = new PlaybackPanel(type, this);
        add(playbackPanel, BorderLayout.CENTER);
        ControlPanel.setInstance(this);
        ControlPanel.getInstance().resizePanel(getHeight());
        add(ControlPanel.getInstance(), BorderLayout.EAST);
        setJMenuBar(new com.kirinpatel.sync.gui.MenuBar(playbackPanel, this));

        setVisible(type == CLIENT);
    }

    class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            ControlPanel.getInstance().resizePanel(getHeight());
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {
            if (Launcher.INSTANCE.connectedUser != null) {
                Launcher.INSTANCE.connectedUser.stop();
            }
        }
    }
}