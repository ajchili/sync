package com.kirinpatel.sync.net

import java.io.Serializable
import java.util.*

data class User constructor(var username: String): Serializable {
    val userID: Long = Math.abs(Random().nextLong())
    var media: Media.MediaData = Media("").data
    var ping: Long = 0

    override fun toString(): String {
        return username
    }
}