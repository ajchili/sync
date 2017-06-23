package com.kirinpatel;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;

/**
 * @author Kirin Patel
 * @version 0.0.1
 * @date 6/23/17
 */
public class Testing {

    private final JFrame frame;

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    public static void StartTest() {
        new NativeDiscovery().discover();
        SwingUtilities.invokeLater(Testing::new);
    }

    public Testing() {
        frame = new JFrame("Testing VLCJ");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        frame.setContentPane(mediaPlayerComponent);
        frame.setVisible(true);
        mediaPlayerComponent.getMediaPlayer().playMedia("http://www.kirinpatel.com/ftp/fairytail/season2/FT%2049%20-%20The%20Day%20of%20the%20Fateful%20Encounter%20L@mBerT.mp4");
    }
}
