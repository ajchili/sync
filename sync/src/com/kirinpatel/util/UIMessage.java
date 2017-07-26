package com.kirinpatel.util;

import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;

import javax.swing.*;


/**
 * Messager class for building messages and handling the calling GUI.
 *
 * @author Kirin Patel
 */
public class UIMessage {
    private final GUI gui;

    public UIMessage(GUI gui) {
        this.gui = gui;
    }

    /**
     * Display a message based on the error with a title. The window is then closed and the opening menu is shown.
     *
     * @param error
     * @param title
     */
    public void showErrorDialogAndExit(Exception error, String title) {
        showErrorDialog(error, title);
        gui.hide();
        new Main();
    }

    /**
     * Display a message based on the error with a title.
     *
     * @param error
     * @param title
     */
    public static void showErrorDialog(Exception error, String title) {
        JOptionPane.showMessageDialog(null, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display a message
     *
     * @param message
     * @param title
     */
    public static void showMessageDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays message dialog with given title and message, then returns
     * user input.
     *
     * @param title   Title
     * @param message Message
     * @return Returns string from input dialog
     */
    public static String getInput(String title, String message) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
    }
}
