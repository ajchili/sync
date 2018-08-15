package com.kirinpatel.sync.util

import java.io.IOException

interface FileSelectorListener {
    fun startedMovingFile()
    fun successfullyMovedFile()
    fun failedToMoveFile(exception: IOException)
    fun fileProgressUpdated(progress: Int)
}