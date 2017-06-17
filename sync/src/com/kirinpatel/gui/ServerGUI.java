package com.kirinpatel.gui;

import com.kirinpatel.net.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author Kirin Patel
 * @version 0.0.1
 * @date 6/16/17
 */
public class ServerGUI extends JFrame {

    public static MediaPanel mediaPanel;
    public static ServerControlPanel serverControlPanel;

    public ServerGUI() {
        super("sync - Server");

        setSize(new Dimension(800, 360));
        setMinimumSize(new Dimension(800, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        mediaPanel = new MediaPanel();
        add(mediaPanel, BorderLayout.CENTER);
        serverControlPanel = new ServerControlPanel();
        serverControlPanel.resizePanel(getWidth(), getHeight());
        add(serverControlPanel, BorderLayout.EAST);

        setVisible(true);
    }

    class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            serverControlPanel.resizePanel(getWidth(), getHeight());
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {
            Server.stop();
            dispose();
        }
    }
}
