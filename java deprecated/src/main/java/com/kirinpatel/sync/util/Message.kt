package com.kirinpatel.sync.util

import java.io.Serializable

data class Message(val messageType : Message.MESSAGE_TYPE, val message : Any?) : Serializable {
    enum class MESSAGE_TYPE(val code : Int) {
        ERROR(-1),
        DISCONNECTING(0),
        CONNECTING(1),
        CONNECTED(2),
        CLOSING(3),
        PING(4),
        CLIENT_NAME(10),
        CONNECTED_CLIENTS(11),
        MEDIA_URL(20),
        MEDIA_TIME(21),
        MEDIA_RATE(22),
        MEDIA_STATE(23),
        MESSAGES(30),
        CLIENT_MESSAGES(31);

        private val messageId : Int = code
    }
}