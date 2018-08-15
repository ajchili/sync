package com.kirinpatel.sync.gui

import com.kirinpatel.sync.net.Media
import com.kirinpatel.sync.util.FileSelector
import com.kirinpatel.sync.util.UIMessage
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.File
import java.nio.file.Paths
import javax.swing.*

class MediaSelector: JFrame("sync") {
    companion object {
        private val listeners: ArrayList<MediaSelectorListener> = ArrayList()

        fun addListener(listener: MediaSelectorListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: MediaSelectorListener) {
            listeners.remove(listener)
        }
    }

    init {
        size = Dimension(225, 115)
        isResizable = false
        layout = BorderLayout()
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent) {

            }

            override fun componentMoved(e: ComponentEvent) {

            }

            override fun componentShown(e: ComponentEvent) {

            }

            override fun componentHidden(e: ComponentEvent) {
                dispose()
                listeners.forEach { listener ->
                    listener.closed()
                }
            }
        })
        setLocationRelativeTo(null)
        JFrame.setDefaultLookAndFeelDecorated(true)

        val buttonPanel = JPanel(GridLayout(2, 1))
        val onlineMedia = JButton("Set Media URL")
        onlineMedia.addActionListener(MediaTypeButtonEvent(this, false))
        buttonPanel.add(onlineMedia)
        val offlineMedia = JButton("Set Media File")
        offlineMedia.addActionListener(MediaTypeButtonEvent(this, true))
        buttonPanel.add(offlineMedia)
        add(buttonPanel, BorderLayout.CENTER)

        isVisible = true

        listeners.forEach { listener ->
            listener.opened()
        }
    }

    internal class MediaTypeButtonEvent constructor(private val view: MediaSelector, private val isOffline: Boolean): ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            view.isVisible = false
            if (isOffline) {
                val mediaFile = FileSelector.getFile()
                if (mediaFile != null) {
                    if (mediaFile.absolutePath.startsWith(File("tomcat/webapps/media").absolutePath)) {
                        listeners.forEach { listener ->
                            listener.mediaSelected(Media(Paths.get(mediaFile.absolutePath)))
                        }
                    } else {
                        UIMessage.showMessageDialog(
                                "The media file that you selected could not be used.",
                                "Error selecting media!")
                    }
                }
            } else {
                SwingUtilities.invokeLater {
                    var mediaURL = JOptionPane.showInputDialog(
                            null,
                            "Please provide the media URL of your media.",
                            "Set media URL",
                            JOptionPane.QUESTION_MESSAGE)
                    if (!mediaURL.isEmpty()) {
                        if (!mediaURL.startsWith("http")) {
                            mediaURL = "http://$mediaURL"
                        }
                        listeners.forEach { listener ->
                            listener.mediaSelected(Media(mediaURL))
                        }
                    } else {
                        UIMessage.showMessageDialog(
                                "The Media URL must be specified!",
                                "Error setting Media URL!")
                    }
                }
            }
        }
    }
}