package com.kirinpatel.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * AudioSettingsGUI class will be used to show the Audio Equalizer to allow a user to set the db boost of each audio
 * frequency band.
 */
public class AudioSettingsGUI extends JFrame {

    private int[] frequencies = new int[10];

    /**
     * Main constructor that will create the AudioSettingsGUI.
     */
    public AudioSettingsGUI() {
        super("Audio Equalizer");

        setSize(new Dimension(450, 300));
        setLayout(new GridLayout(1, 10));
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        for (int i = 32; i <= 16384; i *= 2) {
            final int index = i;

            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(i < 1000 ? i + "" : "" + (i / 1000) + 'K', SwingConstants.CENTER);
            panel.add(label, BorderLayout.NORTH);
            JSlider slider = new JSlider(1, -20, 20, 0);
            slider.setValue(loadSettings(i));
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(4);
            slider.setMinorTickSpacing(1);
            slider.addChangeListener(e -> setBandValue(index, ((JSlider) e.getSource()).getValue()));
            panel.add(slider, BorderLayout.CENTER);
            add(panel);
        }

        setVisible(true);
    }

    /**
     * Saves provided band db boost value.
     *
     * @param index Band index
     * @param value Value of band
     */
    private void setBandValue(int index, int value) {
        int band = 0;
        for (int i = index; i > 32; i /= 2) band++;
        frequencies[band] = value;
        VLCJMediaPlayer.equalizer.setAmp(band, frequencies[band]);
        saveSettings();
    }

    /**
     * Provides db boost of a provided frequency band.
     *
     * @param band Frequency band number
     * @return Returns db boost of provided frequency band
     */
    public static int loadSettings(int band) {
        int location = 0;
        for (int i = band; i > 32; i /= 2) location++;

        File file = new File("equalizer.dat");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                for (int i = 0; i < location; i++) {
                    reader.readLine();
                }

                return Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Saves user equalization settings.
     */
    private void saveSettings() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("equalizer.dat")))) {
            for (int i = 0; i < 10; i++) {
                writer.write(frequencies[i] + "\n");
            }
        } catch(IOException e) {
            // TODO: (ajchili) Properly handle exception
            e.printStackTrace();
        }
    }
}
