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

    private val _ratePerMile = MutableStateFlow(getSavedRatePerMile())
    val ratePerMile: StateFlow<Double> = _ratePerMile.asStateFlow()

    private val _ratePerHour = MutableStateFlow(getSavedRatePerHour())
    val ratePerHour: StateFlow<Double> = _ratePerHour.asStateFlow()

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

    private fun getSavedRatePerMile(): Double {
        return prefs.getFloat("rate_per_mile", 0.725f).toDouble()
    }

    private fun getSavedRatePerHour(): Double {
        return prefs.getFloat("rate_per_hour", 37.50f).toDouble()
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setDefaultStartAddress(address: String) {
        prefs.edit().putString("default_start_address", address.trim()).apply()
        _defaultStartAddress.value = address.trim()
    }

    fun setRatePerMile(rate: Double) {
        prefs.edit().putFloat("rate_per_mile", rate.toFloat()).apply()
        _ratePerMile.value = rate
    }

    fun setRatePerHour(rate: Double) {
        prefs.edit().putFloat("rate_per_hour", rate.toFloat()).apply()
        _ratePerHour.value = rate
    }
}
