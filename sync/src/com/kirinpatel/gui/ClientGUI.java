package com.kirinpatel.gui;

import com.kirinpatel.net.Client;
import com.kirinpatel.util.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author Kirin Patel
 * @version 0.0.3
 * @date 6/16/17
 */
public class ClientGUI extends JFrame {

    public static MediaPanel mediaPanel;
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

        mediaPanel = new MediaPanel();
        add(mediaPanel, BorderLayout.CENTER);
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
            dispose();
            Debug.Log("Client gui closed.", 3);
            Client.stop();
        }
    }
}
