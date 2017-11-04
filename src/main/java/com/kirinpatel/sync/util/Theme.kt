package com.kirinpatel.sync.util

import java.awt.Color

class Theme {
    companion object {
        @JvmStatic
        var isDarkModeEnabled: Boolean = false
        @JvmStatic
        val TEXT_STANDARD: Color = Color.BLACK
        @JvmStatic
        val TEXT_DARK: Color = Color.LIGHT_GRAY
        @JvmStatic
        val BACKGROUND_STANDARD: Color = Color.WHITE
        @JvmStatic
        val BACKGROUND_DARK: Color = Color.BLACK

        @JvmStatic
        fun setIsDarkModeEnabled(isDarkModeEnabled: Boolean) {
            this.isDarkModeEnabled = isDarkModeEnabled
        }
    }
}