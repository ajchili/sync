package com.kirinpatel.gui;

import com.kirinpatel.net.Server;
import com.kirinpatel.net.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import static com.kirinpatel.gui.PlaybackPanel.PANEL_TYPE.*;

public class GUI extends JFrame {

    private final PlaybackPanel.PANEL_TYPE type;

    /**
     * Primary constructor that will create the GUI of sync.
     *
     * @param type Type
     */
    public GUI(PlaybackPanel.PANEL_TYPE type) {
        super(type == SERVER ? "sync - Server" : "sync - Client (" + Client.ipAddress + ":8000)");
        this.type = type;

        setSize(new Dimension(940, 360));
        setMinimumSize(new Dimension(940, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        // TODO: Implements singleton classes so that server/client instances can be created repeatedly
        PlaybackPanel.setInstance(type);
        add(PlaybackPanel.getINSTANCE(), BorderLayout.CENTER);
        ControlPanel.setInstance(this);
        ControlPanel.getInstance().resizePanel(getHeight());
        add(ControlPanel.getInstance(), BorderLayout.EAST);
        setJMenuBar(new MenuBar(PlaybackPanel.getINSTANCE()));

        setVisible(type == CLIENT);
    }

    /**
     * This inner class will handle all resizing and closing of the GUI class.
     */
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
            PlaybackPanel.getINSTANCE().getMediaPlayer().release();
            dispose();
            if (type == SERVER) Server.stop();
            else Client.stop();
        }
    }
}