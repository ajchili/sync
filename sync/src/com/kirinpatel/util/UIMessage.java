package com.kirinpatel.util;

import com.kirinpatel.Main;
import com.kirinpatel.gui.GUI;

import javax.swing.*;

/**
 * Message JOptionPanes that will be displayed to user for message and error
 * events.
 *
 * @author Kirin Patel
 */
public class UIMessage {

    private final int[] TYPE = {JOptionPane.INFORMATION_MESSAGE, JOptionPane.ERROR_MESSAGE};

    /**
     * Display message dialog with given title, message, and dialog type.
     *
     * @param title   Title
     * @param message Message
     * @param type    Message type
     */
    public UIMessage(String title, String message, int type) {
        JOptionPane.showMessageDialog(null, message, title, this.TYPE[type]);
    }

    public static void showErrorDialogueAndExit(String errorMessage, String title, GUI gui) {
        JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.INFORMATION_MESSAGE);
        gui.hide();
        new Main();
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
