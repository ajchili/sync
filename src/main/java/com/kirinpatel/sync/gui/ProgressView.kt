package com.kirinpatel.sync.gui

import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar

class ProgressView constructor(title: String, message: String): JFrame(title) {
    private val progressBar: JProgressBar

    init {
        size = Dimension(400, 100)
        layout = GridLayout(2, 1)
        isResizable = false
        defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
        setLocationRelativeTo(null)

        val messageLabel = JLabel(message)
        add(messageLabel)
        progressBar = JProgressBar(0, 100)
        add(progressBar)

        isVisible = true
    }

    fun setProgress(value: Long, max: Long) {
        progressBar.value = ((value * 100) / max).toInt()
    }
}