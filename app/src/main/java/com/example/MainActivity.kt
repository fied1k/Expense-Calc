package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainScreen
import com.example.ui.PolicyScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current.applicationContext
            val settingsManager = remember { SettingsManager(context) }
            val themeMode by settingsManager.themeMode.collectAsState()

            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                var currentScreen by remember { mutableStateOf("MAIN") }
                val viewModel: CostCalculatorViewModel = viewModel()

                LaunchedEffect(Unit) {
                    viewModel.initDefaultSettings(settingsManager)
                }

                when (currentScreen) {
                    "POLICY" -> PolicyScreen(onBack = { currentScreen = "MAIN" })
                    "SETTINGS" -> SettingsScreen(
                        settingsManager = settingsManager,
                        viewModel = viewModel,
                        onBack = { currentScreen = "MAIN" }
                    )
                    else -> MainScreen(
                        viewModel = viewModel,
                        onShowPolicy = { currentScreen = "POLICY" },
                        onShowSettings = { currentScreen = "SETTINGS" }
                    )
                }
            }
        }
    }
}
