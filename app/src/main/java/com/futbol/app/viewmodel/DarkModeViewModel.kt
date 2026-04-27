package com.futbol.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DarkModeViewModel : ViewModel() {
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggle() {
        isDarkMode = !isDarkMode
    }

    fun setDark(value: Boolean) {
        isDarkMode = value
    }
}
