/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.util;

import javax.swing.JOptionPane;

/**
 *
 * @author Kirin Patel
 * @version 1.0
 */
public class UIMessage {
    
    private final int[] type = { JOptionPane.INFORMATION_MESSAGE, JOptionPane.ERROR_MESSAGE };
    
    /**
     * Display message dialog with given title, message, and dialog type.
     * 
     * @param title Title
     * @param message Message
     * @param type Type
     */
    public UIMessage(String title, String message, int type) {
        JOptionPane.showMessageDialog(null, title, message, this.type[type]);
    }
}
