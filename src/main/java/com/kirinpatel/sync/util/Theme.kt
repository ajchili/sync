package com.kirinpatel.sync.util

import java.awt.Color

class Theme {
    companion object {
        var isDarkModeEnabled: Boolean = false
        var TEXT_STANDARD: Color = Color.BLACK
        var TEXT_DARK: Color = Color.LIGHT_GRAY
        var BACKGROUND_STANDARD: Color = Color.WHITE
        var BACKGROUND_DARK: Color = Color.BLACK

        fun setIsDarkModeEnabled(isDarkModeEnabled: Boolean) {
            this.isDarkModeEnabled = isDarkModeEnabled
        }
    }
}