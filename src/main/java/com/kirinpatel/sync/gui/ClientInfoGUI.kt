package com.kirinpatel.sync.gui

import com.kirinpatel.sync.net.Server
import com.kirinpatel.sync.net.User
import java.awt.GridLayout
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

class ClientInfoGUI constructor(val user: User): JFrame("Client info") {
    val ping = JLabel("Ping: " + user.ping + " ms")
    val mediaState = JLabel("Playback State: " + if (user.media.isPaused) "Paused" else "Playing")
    val mediaTime = JLabel("Playback Time: "
            + user.media.formattedTime
            + ":" + user.media.currentTime % 1000)
    internal val updateUIThread = UpdateUIThread()

    init {
        isResizable = false
        layout = GridLayout(6, 1)
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        addComponentListener(ClientInfoComponentListener())
        setLocationRelativeTo(null)

        add(JLabel("Username: " + user.username))
        add(JLabel("UserID: " + user.userID))
        add(ping)
        add(mediaTime)
        add(mediaState)
        val disconnectUser = JButton("Kick Client")
        disconnectUser.addActionListener { _ ->
            Server.kickUser(user)
            ControlPanel.isUserDisplayShown = false
            updateUIThread.stop()
            dispose()
        }
        add(disconnectUser)
        pack()

        Thread(updateUIThread).start()

        isVisible = true
    }

    internal inner class ClientInfoComponentListener : ComponentListener {

        override fun componentResized(e: ComponentEvent) {

        }

        override fun componentMoved(e: ComponentEvent) {

        }

        override fun componentShown(e: ComponentEvent) {

        }

        override fun componentHidden(e: ComponentEvent) {
            ControlPanel.isUserDisplayShown = false
            updateUIThread.stop()
            dispose()
        }
    }

    internal inner class UpdateUIThread : Runnable {
        private var isRunning = true

        override fun run() {
            while (isRunning) {
                try {
                    Thread.sleep(250)
                    ping.text = "Ping: " + user.ping + " ms"
                    mediaTime.text = ("Playback Time: "
                            + user.media.formattedTime
                            + ":" + user.media.currentTime % 1000)
                    mediaState.text = "Playback State: " + if (user.media.isPaused) "Paused" else "Playing"
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }

            }
        }

        fun stop() {
            isRunning = false
        }
    }
}