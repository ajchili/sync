package com.kirinpatel.sync.gui

import com.kirinpatel.sync.util.UIMessage
import uk.co.caprica.vlcj.player.Equalizer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*

class AudioSettingsGUI: JFrame("Audio Equalizer") {
    companion object {
        private val PATH: Path? = Paths.get("equalizer.dat")
        @JvmStatic
        lateinit var equalizer: Equalizer

        @JvmStatic
        fun loadSettings() {
            if (Files.exists(PATH)) {
                try {
                    Files.newBufferedReader(PATH).use { reader ->
                        for (i in 0..9) {
                            val boost = Integer.parseInt(reader.readLine())
                            equalizer.setAmp(i, boost.toFloat())
                        }
                    }
                } catch (e: IOException) {
                    UIMessage.showErrorDialog(e, "Couldn't load equalizer settings!")
                }
            }
        }
    }

    init {
        size = Dimension(450, 300)
        layout = GridLayout(1, 10)
        isResizable = false
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        setLocationRelativeTo(null)

        for (i in 0..9) {
            val hzBand = 32 * Math.pow(2.0, i.toDouble()).toInt()
            val panel = JPanel(BorderLayout())
            val label = JLabel(
                    if (hzBand < 1000)
                        Integer.toString(hzBand)
                    else
                        Integer.toString(hzBand / 1000) + 'K',
                    SwingConstants.CENTER)
            panel.add(label, BorderLayout.NORTH)
            val slider = JSlider(1, -20, 20, 0)
            slider.value = equalizer.getAmp(i).toInt()
            slider.paintTicks = true
            slider.paintLabels = true
            slider.majorTickSpacing = 4
            slider.minorTickSpacing = 1
            slider.addChangeListener { e -> setBandValue(i, (e.source as JSlider).value) }
            panel.add(slider, BorderLayout.CENTER)
            add(panel)
        }

        isVisible = true
    }

    private fun setBandValue(index: Int, value: Int) {
        equalizer.setAmp(index, value.toFloat())
        saveSettings()
    }

    private fun saveSettings() {
        try {
            Files.newBufferedWriter(PATH).use { writer ->
                for (i in 0..9) {
                    writer.write(equalizer.getAmp(i).toInt().toString() + "\n")
                }
            }
        } catch (e: IOException) {
            UIMessage.showErrorDialog(e, "Couldn't save equalizer settings!")
        }

    }
}