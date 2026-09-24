package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _defaultStartAddress = MutableStateFlow(getSavedDefaultStartAddress())
    val defaultStartAddress: StateFlow<String> = _defaultStartAddress.asStateFlow()

    private fun getSavedThemeMode(): ThemeMode {
        return when (prefs.getString("theme_mode", "SYSTEM")) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    private fun getSavedDefaultStartAddress(): String {
        return prefs.getString("default_start_address", "") ?: ""
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setDefaultStartAddress(address: String) {
        prefs.edit().putString("default_start_address", address.trim()).apply()
        _defaultStartAddress.value = address.trim()
    }
}
