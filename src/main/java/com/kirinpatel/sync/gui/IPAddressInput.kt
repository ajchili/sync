package com.kirinpatel.sync.gui

import com.kirinpatel.sync.net.Client
import com.kirinpatel.sync.util.UIMessage
import com.kirinpatel.sync.util.getPreviousAddresses
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.*

class IPAddressInput : JFrame("sync") {

    private val ipField: JTextField
    private lateinit var listener: IPAddressInputListener

    init {
        size = Dimension(300, 115)
        isResizable = true
        layout = GridLayout(3, 1)
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent) {

            }

            override fun componentMoved(e: ComponentEvent) {

            }

            override fun componentShown(e: ComponentEvent) {

            }

            override fun componentHidden(e: ComponentEvent) {
                dispose()
                listener.closed()
            }
        })
        setLocationRelativeTo(null)
        setDefaultLookAndFeelDecorated(true)

        val label = JLabel("Please enter or select the server you want to join")
        label.horizontalAlignment = SwingConstants.CENTER
        add(label)

        val ipPanel = JPanel(GridLayout(1, 2))

        ipField = JTextField()
        ipField.addActionListener(ServerIPAddressInputActionListener())
        ipPanel.add(ipField)
        val ipBox = JComboBox(getPreviousAddresses().toArray())
        ipBox.selectedItem = null
        ipBox.addItemListener { e ->
            Client(e.item.toString())
            dispose()
            isVisible = false
        }
        ipPanel.add(ipBox)

        add(ipPanel)

        val connect = JButton("Connect")
        connect.addActionListener(ServerIPAddressInputActionListener())
        add(connect)

        isVisible = true
    }

    fun setListener(listener: IPAddressInputListener) {
        this.listener = listener
    }

    internal inner class ServerIPAddressInputActionListener : ActionListener {

        override fun actionPerformed(e: ActionEvent) {
            if (!ipField.text.isEmpty()) {
                Client(ipField.text)
                dispose()
                listener.serverSelected()
            } else {
                UIMessage.showMessageDialog(
                        "No IP address provided! An IP address must be provided!",
                        "Error with provided IP address!")
            }
        }
    }
}