package com.kirinpatel.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class AudioSettingsGUI extends JFrame {

    private int[] frequencies = new int[10];

    public AudioSettingsGUI() {
        super("Audio Settings");

        setSize(new Dimension(600, 600));
        setLayout(new GridLayout(1, 10));
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        for (int i = 32; i <= 16384; i *= 2) {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(i < 1000 ? i + "" : "" + (i / 1000) + 'K');
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(label, BorderLayout.NORTH);
            JSlider slider = new JSlider(1, -12, 12, 0);
            slider.setValue(loadSettings(i));
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(3);
            slider.setMinorTickSpacing(1);
            final int j = i;
            slider.addChangeListener(e -> {
                int band = 0;
                for (int k = j; k > 32; k /= 2) band++;
                frequencies[band] = ((JSlider) e.getSource()).getValue();
                saveSettings();
            });
            panel.add(slider, BorderLayout.CENTER);
            add(panel);

        }

        setVisible(true);
    }

    private int loadSettings(int band) {
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
                System.out.println(frequencies[i]);
                writer.write(frequencies[i] + "\n");
            }

            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
