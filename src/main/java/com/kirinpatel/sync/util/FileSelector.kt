package com.kirinpatel.sync.util

import com.kirinpatel.sync.gui.ProgressView
import com.kirinpatel.sync.net.Server
import java.awt.Component
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.swing.JFileChooser
import javax.swing.UIManager

fun getFile(parent : Component) : File? {
    val mediaSelector = JFileChooser("tomcat/webapps/media")
    mediaSelector.fileSelectionMode = JFileChooser.FILES_ONLY
    val returnVal: Int = mediaSelector.showOpenDialog(parent)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        val selectedFile = mediaSelector.selectedFile ?: return null
        return if (selectedFile.absolutePath.startsWith(File("tomcat/webapps/media").absolutePath)) {
            selectedFile
        } else {
            moveFile(selectedFile)
        }
    } else {
        return null
    }
}
private fun moveFile(selectedFile : File) : File {
    val newFile = File("tomcat/webapps/media/" + selectedFile.name)
    val progressView = ProgressView("Moving media",
            "Please wait while your media is moved to the proper folder.")
    Thread {
        try {
            FileInputStream(selectedFile).use { inStream ->
                FileOutputStream(newFile).use { outStream ->
                    val buffer = ByteArray(1024)
                    var length: Int = inStream.read(buffer)
                    while (length > 0) {
                        outStream.write(buffer, 0, length)
                        length = inStream.read(buffer)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }.start()
    Thread {
        Server.setEnabled(false)
        val timeout = System.currentTimeMillis()
        var time: Long = 0
        while (newFile.length() < selectedFile.length()) {
            time = System.currentTimeMillis()
            progressView.setProgress(newFile.length(), selectedFile.length())
            if (timeout + 1000 * 300 < time) {
                UIMessage.showMessageDialog(
                        "Your media was unable to be moved." +
                                "\nPlease try to manually move your media to the Tomcat directory.",
                        "Unable to move media")
                break
            }
        }
        progressView.dispose()
        Server.setEnabled(true)
        if (timeout + 1000 * 300 > time) {
            UIMessage.showMessageDialog(
                    "Your media has been moved to the\nTomcat folder and is ready for playback.",
                    "Your media is ready")
        }
    }.start()
    return newFile
}