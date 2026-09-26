package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.CostCalculatorViewModel
import com.example.NetworkClient
import com.example.NominatimResult
import com.example.SettingsManager
import com.example.ThemeMode
import com.example.parseCleanDouble
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    viewModel: CostCalculatorViewModel,
    onBack: () -> Unit
) {
    val currentTheme by settingsManager.themeMode.collectAsState()
    val defaultAddress by settingsManager.defaultStartAddress.collectAsState()
    val ratePerMile by settingsManager.ratePerMile.collectAsState()
    val ratePerHour by settingsManager.ratePerHour.collectAsState()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var addressQuery by remember { mutableStateOf(defaultAddress) }
    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var mileRateText by remember { mutableStateOf(String.format(Locale.US, "%.3f", ratePerMile)) }
    var hourRateText by remember { mutableStateOf(String.format(Locale.US, "%.2f", ratePerHour)) }

    LaunchedEffect(defaultAddress) {
        addressQuery = defaultAddress
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                suggestions = emptyList()
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            Column {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select light, dark, or follow system default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple(ThemeMode.SYSTEM, "System", Icons.Default.PhoneAndroid),
                        Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                        Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode)
                    )

                    themeOptions.forEach { (mode, label, icon) ->
                        val selected = currentTheme == mode
                        FilterChip(
                            selected = selected,
                            onClick = { settingsManager.setThemeMode(mode) },
                            label = { Text(label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider()

            // Rate Settings Section
            Column {
                Text(
                    text = "Travel Rates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Customize the mileage rate and hourly rate used for calculations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = mileRateText,
                        onValueChange = { mileRateText = it },
                        label = { Text("Rate Per Mile ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = hourRateText,
                        onValueChange = { hourRateText = it },
                        label = { Text("Rate Per Hour ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val parsedMile = parseCleanDouble(mileRateText)
                        val parsedHour = parseCleanDouble(hourRateText)

                        if (parsedMile != null && parsedMile >= 0) {
                            settingsManager.setRatePerMile(parsedMile)
                        }
                        if (parsedHour != null && parsedHour >= 0) {
                            settingsManager.setRatePerHour(parsedHour)
                        }
                        Toast.makeText(context, "Travel rates saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Rates")
                }
            }

            HorizontalDivider()

            // Default Starting Address Section
            Column {
                Text(
                    text = "Default Starting Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set a default address to automatically pre-fill when the app opens.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (defaultAddress.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Default:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = defaultAddress,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            IconButton(onClick = {
                                settingsManager.setDefaultStartAddress("")
                                addressQuery = ""
                                Toast.makeText(context, "Default starting address cleared", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear Default", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AutocompleteField(
                    value = addressQuery,
                    onValueChange = { q ->
                        addressQuery = q
                        searchJob?.cancel()
                        if (q.length >= 3) {
                            searchJob = scope.launch {
                                delay(500)
                                try {
                                    suggestions = NetworkClient.nominatimApi.search(q)
                                } catch (e: Exception) {
                                    suggestions = emptyList()
                                }
                            }
                        } else {
                            suggestions = emptyList()
                        }
                    },
                    placeholder = "Enter address or ZIP (e.g., 66204)",
                    suggestions = suggestions,
                    onSuggestionSelected = { res ->
                        addressQuery = res.cleanName
                        suggestions = emptyList()
                        focusManager.clearFocus()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        suggestions = emptyList()
                        if (addressQuery.isNotBlank()) {
                            settingsManager.setDefaultStartAddress(addressQuery)
                            viewModel.applyDefaultStartAddress(addressQuery)
                            Toast.makeText(context, "Default starting address saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = addressQuery.isNotBlank() && addressQuery != defaultAddress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save as Default Address")
                }
            }
        }
    }
}
