package com.kirinpatel;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.*;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Created by Kirin on 6/24/2017.
 */
public class Testing {
    private static int width = 1280;

    private static int height = 720;

    private final JFrame frame;

    private final JPanel videoSurface;

    private final BufferedImage image;
    private BufferedImage scale;

    private final DirectMediaPlayerComponent mediaPlayerComponent;

    public Testing() {
        new NativeDiscovery().discover();
        frame = new JFrame("Direct Media Player");
        frame.setBounds(100, 100, width, height);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        videoSurface = new VideoSurfacePanel();
        frame.setContentPane(videoSurface);
        image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(width, height);
        BufferFormatCallback bufferFormatCallback = (sourceWidth, sourceHeight) -> new RV32BufferFormat(width, height);
        mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new TutorialRenderCallbackAdapter();
            }
        };
        frame.setVisible(true);
        mediaPlayerComponent.getMediaPlayer().setVolume(0);
        mediaPlayerComponent.getMediaPlayer().playMedia("http://www.kirinpatel.com/ftp/fairytail/season2/FT%2049%20-%20The%20Day%20of%20the%20Fateful%20Encounter%20L@mBerT.mp4");
    }

    public class VideoSurfacePanel extends JPanel {

        private VideoSurfacePanel() {
            setBackground(Color.black);
            setOpaque(true);
            setPreferredSize(new Dimension(width, height));
            setMaximumSize(new Dimension(1280, 720));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.drawImage(scale, null, 0, 0);
        }
    }

    private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

        private TutorialRenderCallbackAdapter() {
            super(new int[width * height]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            // Simply copy buffer to the image and repaint
            float xScale = (float) videoSurface.getWidth() / width;
            float yScale = (float) videoSurface.getHeight() / height;
            image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
            BufferedImage after = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(xScale, yScale);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            scale = scaleOp.filter(image, after);
            videoSurface.repaint();
        }
    }
}
