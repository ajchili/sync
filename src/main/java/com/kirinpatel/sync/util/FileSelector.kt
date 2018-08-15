package com.kirinpatel.sync.util

import com.kirinpatel.sync.gui.ProgressView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.swing.JFileChooser

class FileSelector {
    companion object {
        private var isIterating: Boolean = false
        private val listeners: ArrayList<FileSelectorListener> = ArrayList()

        fun addListener(listener: FileSelectorListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: FileSelectorListener) {
            while (isIterating) {
                Thread.sleep(50)
            }
            listeners.remove(listener)
        }

        fun getFile() : File? {
            val mediaSelector = JFileChooser("tomcat/webapps/media")
            mediaSelector.fileSelectionMode = JFileChooser.FILES_ONLY
            val returnVal: Int = mediaSelector.showOpenDialog(null)
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
            var progress: Int = -1
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
                    isIterating = true
                    listeners.forEach { listener ->
                        listener.failedToMoveFile(e)
                    }
                    isIterating = false
                }
            }.start()
            ProgressView()
            Thread {
                isIterating = true
                listeners.forEach { listener ->
                    listener.startedMovingFile()
                }
                isIterating = false
                val timeout = System.currentTimeMillis()
                var time: Long
                while (newFile.length() < selectedFile.length()) {
                    time = System.currentTimeMillis()
                    if (progress != (newFile.length() * 100 / selectedFile.length()).toInt()) {
                        isIterating = true
                        listeners.forEach { listener ->
                            listener.fileProgressUpdated((newFile.length() * 100 / selectedFile.length()).toInt())
                        }
                        isIterating = false
                    }
                    if (timeout + 1000 * 300 < time) {
                        isIterating = true
                        listeners.forEach {listener ->
                            listener.failedToMoveFile(IOException("File took too long to move."))
                        }
                        isIterating = false
                        break
                    }
                }
                isIterating = true
                listeners.forEach { listener ->
                    listener.successfullyMovedFile()
                }
                isIterating = false
            }.start()
            return newFile
        }
    }
}