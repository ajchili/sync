package com.kirinpatel.sync.gui;

import javax.swing.*;
import java.awt.*;

public class ProgressView extends JFrame {

    private JProgressBar progressBar;

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

    public void setProgress(long value, long max) {
        progressBar.setValue((int) ((value * 100) / max));
    }
}
