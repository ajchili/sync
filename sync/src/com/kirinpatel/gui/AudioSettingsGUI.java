package com.kirinpatel.gui;

import com.kirinpatel.vlc.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class AudioSettingsGUI extends JFrame {

    private int[] frequencies = new int[10];

    public AudioSettingsGUI() {
        super("Audio Equalizer");

        setSize(new Dimension(450, 300));
        setLayout(new GridLayout(1, 10));
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        for (int i = 32; i <= 16384; i *= 2) {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(i < 1000 ? i + "" : "" + (i / 1000) + 'K', SwingConstants.CENTER);
            panel.add(label, BorderLayout.NORTH);
            JSlider slider = new JSlider(1, -20, 20, 0);
            slider.setValue(loadSettings(i));
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(4);
            slider.setMinorTickSpacing(1);
            final int j = i;
            slider.addChangeListener(e -> {
                int band = 0;
                for (int k = j; k > 32; k /= 2) band++;
                frequencies[band] = ((JSlider) e.getSource()).getValue();
                MediaPlayer.equalizer.setAmp(band, frequencies[band]);
                saveSettings();
            });
            panel.add(slider, BorderLayout.CENTER);
            add(panel);
        }

        setVisible(true);
    }

    public static int loadSettings(int band) {
        int location = 0;
        for (int i = band; i > 32; i /= 2) location++;

        File file = new File("equalizer.dat");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                for (int i = 0; i < location; i++) {
                    reader.readLine();
                }

                return Integer.parseInt(reader.readLine());
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void saveSettings() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(new File("equalizer.dat")));
            for (int i = 0; i < 10; i++) {
                writer.write(frequencies[i] + "\n");
            }

            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
