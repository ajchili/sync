/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel;

import com.kirinpatel.gui.Window;

/**
 * Main class that will run the application.
 * 
 * @author Kirin Patel
 * @version 1.0
 * @see com.kirinpatel.gui.Window
 */
public class Main {
    
    public static Window window;
    
    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        window = new Window("sync", 0);
    }
}
