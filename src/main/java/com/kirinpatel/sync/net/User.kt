package com.kirinpatel.sync.net

import java.io.Serializable
import java.util.*

class User constructor(var username: String): Serializable {
    val userID: Long = Math.abs(Random().nextLong())
    var media: Media.MediaData = Media("").data
    var ping: Long = 0

    override fun toString(): String {
        return username
    }

    override fun equals(other: Any?): Boolean {
        return if (other is User) {
            username == other.username && userID == other.userID
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + media.hashCode()
        result = 31 * result + ping.hashCode()
        return result
    }
}