package com.kirinpatel.sync.net

interface NetworkUsers {
    fun stop()
    fun sendMessage(message: String)
    fun getUser() : User
}