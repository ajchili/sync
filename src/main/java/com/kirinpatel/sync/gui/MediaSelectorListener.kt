package com.kirinpatel.sync.gui

import com.kirinpatel.sync.net.Media

interface MediaSelectorListener {
    fun opened()
    fun mediaSelected(media: Media)
    fun closed()
}