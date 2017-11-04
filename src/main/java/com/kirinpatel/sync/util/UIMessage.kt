package com.kirinpatel.sync.util

import com.kirinpatel.sync.gui.GUI
import javax.swing.JOptionPane

class UIMessage(g : GUI) {
    private val gui : GUI = g

    fun showErrorDialogAndExit(error : Exception, title : String) {
        showErrorDialog(error, title)
    }

    companion object {
        @JvmStatic
        fun showErrorDialog(error : Exception, title : String) {
            JOptionPane.showMessageDialog(null, error.message, title, JOptionPane.ERROR_MESSAGE)
        }

        @JvmStatic
        fun showMessageDialog(message: String, title: String) {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
        }

        @JvmStatic
        fun getInput(title: String, message: String) : String {
            return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE)
        }
    }
}