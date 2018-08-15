package com.kirinpatel.sync.gui

import com.kirinpatel.sync.util.FileSelector
import com.kirinpatel.sync.util.FileSelectorListener
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.IOException
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

class ProgressView: JFrame("Moving media...") {
    private val progressBar: JProgressBar
    private val fileMovementListener: FileMovementListener = FileMovementListener(this)

    init {
        size = Dimension(400, 100)
        layout = GridLayout(2, 1)
        isResizable = false
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        setLocationRelativeTo(null)
        addComponentListener(object: ComponentListener {
            override fun componentMoved(e: ComponentEvent?) {

            }

            override fun componentResized(e: ComponentEvent?) {

            }

            override fun componentHidden(e: ComponentEvent?) {
                dispose()
                FileSelector.removeListener(fileMovementListener)
            }

            override fun componentShown(e: ComponentEvent?) {

            }
        })

        val messageLabel = JLabel("Please wait while your media is moved to the proper folder.")
        add(messageLabel)
        progressBar = JProgressBar(0, 100)
        progressBar.isStringPainted = true
        add(progressBar)

        FileSelector.addListener(fileMovementListener)

        isVisible = true
    }

    internal class FileMovementListener(private val view: ProgressView) : FileSelectorListener {
        override fun startedMovingFile() {

        }

        override fun successfullyMovedFile() {
            view.dispose()
            FileSelector.removeListener(this)
        }

        override fun failedToMoveFile(exception: IOException) {
            view.dispose()
            FileSelector.removeListener(this)
        }

        override fun fileProgressUpdated(progress: Int) {
            SwingUtilities.invokeLater {
                view.progressBar.string = progress.toString() + "%"
                view.progressBar.value = progress
            }
        }
    }
}