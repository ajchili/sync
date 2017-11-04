package com.kirinpatel.sync.util

import java.awt.Color

var isDarkModeEnabled: Boolean = false
val TEXT_STANDARD: Color = Color.BLACK
val TEXT_DARK: Color = Color.LIGHT_GRAY
val BACKGROUND_STANDARD: Color = Color.WHITE
val BACKGROUND_DARK: Color = Color.BLACK
fun setIsDarkModeEnabled(darkMode: Boolean) {
    isDarkModeEnabled = darkMode
}