package com.kirinpatel.sync.utils;

import com.kirinpatel.sync.Launcher;
import com.kirinpatel.sync.gui.GUI;

import javax.swing.*;

public class UIMessage {
    private final GUI gui;

    public UIMessage(GUI gui) {
        this.gui = gui;
    }

    public void showErrorDialogAndExit(Exception error, String title) {
        showErrorDialog(error, title);
        gui.hide();
        Launcher.INSTANCE.dispose();
    }

    public static void showErrorDialog(Exception error, String title) {
        JOptionPane.showMessageDialog(null, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showMessageDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static String getInput(String title, String message) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
    }
}
