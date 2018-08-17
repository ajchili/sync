package com.kirinpatel.sync.gui;

import com.kirinpatel.sync.util.FileSelector;
import com.kirinpatel.sync.util.FileSelectorListener;
import com.kirinpatel.sync.util.UIMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;

import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.CLIENT;
import static com.kirinpatel.sync.gui.PlaybackPanel.PANEL_TYPE.SERVER;

public class GUI extends JFrame {

    public PlaybackPanel playbackPanel;

    public GUI(PlaybackPanel.PANEL_TYPE type) {
        super(type == SERVER ? "sync - Server" : "sync - Client");

        setSize(new Dimension(940, 360));
        setMinimumSize(new Dimension(940, 360));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        addComponentListener(new ResizeListener());

        playbackPanel = new PlaybackPanel(type, this);
        add(playbackPanel, BorderLayout.CENTER);
        ControlPanel.setInstance(this);
        ControlPanel.getInstance().resizePanel(getHeight());
        add(ControlPanel.getInstance(), BorderLayout.EAST);
        setJMenuBar(new com.kirinpatel.sync.gui.MenuBar(playbackPanel, this));

        FileSelector.Companion.addListener(new FileMovementListener());

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
            if (Launcher.connectedUser != null) {
                Launcher.connectedUser.stop();
            }
        }
    }

    class FileMovementListener implements FileSelectorListener {

        @Override
        public void startedMovingFile() {
            setEnabled(false);
        }

        @Override
        public void successfullyMovedFile() {
            setEnabled(true);
            UIMessage.showMessageDialog(
                    "Your media has been moved to the\nTomcat folder and is ready for playback.",
                    "Your media is ready");
        }

        @Override
        public void failedToMoveFile(IOException exception) {
            setEnabled(true);
            UIMessage.showErrorDialog(exception, "Unable to move media");
        }

        @Override
        public void fileProgressUpdated(int progress) {

        }
    }
}