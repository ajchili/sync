package com.kirinpatel.sync.net

interface NetworkUser {
    fun stop()
    fun sendMessage(message: String)
    fun getUser() : User
}