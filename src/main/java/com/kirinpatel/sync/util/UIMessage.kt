package com.kirinpatel.sync.util

import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class UIMessage {
    companion object {
        @JvmStatic
        fun showErrorDialog(error : Exception, title : String) {
            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(null, error.message, title, JOptionPane.ERROR_MESSAGE)
            }
        }

        @JvmStatic
        fun showMessageDialog(message: String, title: String) {
            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
            }
        }
    }
}