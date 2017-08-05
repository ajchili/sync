package com.kirinpatel.sync.gui;

import javax.swing.*;
import java.awt.*;

/**
 * The ProgressView class is used to display a UI element to visualize the speed at which a file is being moved to
 * the Tomcat media directory.
 */
public class ProgressView extends JFrame {

    private JProgressBar progressBar;

    /**
     * Main constructor that will create the ProgressView.
     *
     * @param title Title
     * @param message Message to be displayed
     */
    public ProgressView(String title, String message) {
        super(title);

        setSize(new Dimension(400, 100));
        setLayout(new GridLayout(2, 1));
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel messageLabel = new JLabel(message);
        add(messageLabel);
        progressBar = new JProgressBar(0, 100);
        add(progressBar);

        setVisible(true);
    }

    /**
     * Sets progress of ProgressView.
     *
     * @param value Current value
     * @param max Max value
     */
    public void setProgress(long value, long max) {
        progressBar.setValue((int) ((value * 100) / max));
    }
}
