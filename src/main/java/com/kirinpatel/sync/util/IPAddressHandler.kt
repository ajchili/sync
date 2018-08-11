package com.kirinpatel.sync.util

import com.google.common.collect.ImmutableList
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.Files
import java.nio.file.Paths

fun saveIPAddress(ipAddress: String) {
    if (getPreviousAddresses().contains(ipAddress)) {
        return
    }
    val dataPath = Paths.get("launcherData.dat")
    try {
        Files.write(
                dataPath,
                listOf(ipAddress),
                UTF_8,
                if (Files.exists(dataPath)) APPEND else CREATE)
    } catch (e: IOException) {
        UIMessage.showErrorDialog(e, "Couldn't save IP address!")
    }

}

fun getPreviousAddresses(): ImmutableList<String> {
    val dataPath = Paths.get("launcherData.dat")
    return try {
        if (Files.exists(dataPath)) ImmutableList.copyOf(Files.readAllLines(dataPath)) else ImmutableList.of()
    } catch (e: IOException) {
        UIMessage.showErrorDialog(e, "Unable to load previous servers!")
        ImmutableList.of()
    }
}