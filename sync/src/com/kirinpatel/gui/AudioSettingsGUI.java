package com.kirinpatel.gui;

import com.kirinpatel.util.UIMessage;
import uk.co.caprica.vlcj.player.Equalizer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AudioSettingsGUI class will be used to show the Audio Equalizer to allow a user to set the db boost of each audio
 * frequency band.
 */
class AudioSettingsGUI extends JFrame {
    private static final Path SETTINGS_PATH = Paths.get("equalizer.dat");

    private static Equalizer equalizer;

    /**
     * Main constructor that will create the AudioSettingsGUI.
     */
    AudioSettingsGUI() {
        super("Audio Equalizer");

        setSize(new Dimension(450, 300));
        setLayout(new GridLayout(1, 10));
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        for (int i = 0; i < 10; i++) {
            final int hzBand = 32 * (int) Math.pow(2, i);
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(
                    hzBand < 1000
                    ? Integer.toString(hzBand)
                    : Integer.toString(hzBand / 1000) + 'K',
                    SwingConstants.CENTER);
            panel.add(label, BorderLayout.NORTH);
            JSlider slider = new JSlider(1, -20, 20, 0);
            slider.setValue((int) equalizer.getAmp(i));
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(4);
            slider.setMinorTickSpacing(1);
            final int index = i;
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
        equalizer.setAmp(index, value);
        saveSettings();
    }

    /**
     * Load the settings into the equalizer
     */
    static void loadSettings() {
        if (Files.exists(SETTINGS_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(SETTINGS_PATH)) {
                for (int i = 0; i < 10; i++) {
                    int boost = Integer.parseInt(reader.readLine());
                    equalizer.setAmp(i, boost);
                }
            } catch (IOException e) {
                UIMessage.showErrorDialog(e, "Couldn't load equalizer settings!");
            }
        }
    }

    /**
     * Saves user equalization settings.
     */
    private void saveSettings() {
        try (BufferedWriter writer = Files.newBufferedWriter(SETTINGS_PATH)) {
            for (int i = 0; i < 10; i++) {
                writer.write((int) equalizer.getAmp(i) + "\n");
            }
        } catch(IOException e) {
            UIMessage.showErrorDialog(e, "Couldn't save equalizer settings!");
        }
    }

    static void setEqualizer(Equalizer equalizer) {
        AudioSettingsGUI.equalizer = equalizer;
    }
}
