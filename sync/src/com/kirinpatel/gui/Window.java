/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.gui;

import javax.swing.*;

/**
 *
 * @author Kirin Patel
 * @version 0.1
 */
public class Window extends JFrame {
    
    public Window(String title, int type) {
        super(title);
        
        setSize(640, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        switch(type) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            default:
        }
        
        setVisible(true);
    }
}
