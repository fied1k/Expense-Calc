package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                    else -> CostCalculatorApp(
                        viewModel = viewModel,
                        onShowPolicy = { currentScreen = "POLICY" },
                        onShowSettings = { currentScreen = "SETTINGS" }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Rates and Policy") },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PolicySection(
                title = "Mileage",
                content = "72.5 cents per mile round trip minus 30 miles. Do not use WM for your distance, it is often incorrect. Instead input the starting and stopping point into GPS or Google for accurate mileage."
            )
            PolicySection(
                title = "Windshield time",
                content = "$37.50 per hour. Please use a GPS app and schedule a drive for the correct time. Travel time can vary significantly depending on the time of day."
            )
            PolicySection(
                title = "Flight Pay",
                content = "$240 to fly out and back home the same day.\n\n$480 to fly out and return the next day, plus a $65 per diem. Please remember, a per diem is only valid for overnights stays."
            )
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostCalculatorApp(
    viewModel: CostCalculatorViewModel = viewModel(),
    onShowPolicy: () -> Unit,
    onShowSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    viewModel.dismissAutocomplete()
                })
            },
        topBar = {
            TopAppBar(
                title = { Text("Cost Calculator", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onShowSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = onShowPolicy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Travel Rates and Policy", fontWeight = FontWeight.Bold)
                }
            }

            // Error Message
            if (state.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Instruction
            item {
                Text(
                    text = "Enter address, city, or ZIP to calculate time and distance (still in development so accuracy may vary) OR enter manually below",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Route Auto-Calculator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Route Auto-Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Enter Address or US ZIP", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AutocompleteField(
                            value = state.startQuery,
                            onValueChange = { viewModel.updateStartQuery(it) },
                            placeholder = "Start (e.g., 66204)",
                            suggestions = state.startAutocomplete,
                            onSuggestionSelected = { viewModel.selectStartLocation(it) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        AutocompleteField(
                            value = state.endQuery,
                            onValueChange = { viewModel.updateEndQuery(it) },
                            placeholder = "End (e.g., Kansas City, MO)",
                            suggestions = state.endAutocomplete,
                            onSuggestionSelected = { viewModel.selectEndLocation(it) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.calculateRoute()
                            })
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { 
                                focusManager.clearFocus()
                                viewModel.calculateRoute() 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading && state.startQuery.isNotBlank() && state.endQuery.isNotBlank()
                        ) {
                            Text(if (state.isLoading) "Calculating..." else "Calculate Route")
                        }
                    }
                }
            }

            // Manual Inputs
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = state.baseMiles,
                        onValueChange = { viewModel.updateMiles(it) },
                        label = { Text("Miles (One Way)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.formatMilesOnBlur()
                                }
                            }
                    )
                    OutlinedTextField(
                        value = state.baseHours,
                        onValueChange = { viewModel.updateHours(it) },
                        label = { Text("Hours (One Way)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.formatHoursOnBlur()
                                }
                            }
                    )
                }
            }

            // Additional Expenses
            item {
                Column {
                    Text("Additional Expenses (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (state.additionalExpenses.isEmpty()) {
                        Text(
                            text = "No extra expenses added yet (e.g. tolls, parking, or per diem).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    state.additionalExpenses.forEachIndexed { index, expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = expense.name,
                                onValueChange = { viewModel.updateExpense(index, it, expense.cost) },
                                placeholder = { Text("Item (e.g. Tolls)") },
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = expense.cost,
                                onValueChange = { viewModel.updateExpense(index, expense.name, it) },
                                placeholder = { Text("$0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(
                                onClick = { viewModel.removeExpense(index) },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.addExpense() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Expense Item")
                    }
                }
            }

            // Breakdown
            item {
                BreakdownSection(
                    state = state,
                    ratePerMile = viewModel.RATE_PER_MILE,
                    ratePerHour = viewModel.RATE_PER_HOUR,
                    onCopy = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Cost Breakdown", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun AutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suggestions: List<NominatimResult>,
    onSuggestionSelected: (NominatimResult) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column {
                    suggestions.forEach { suggestion ->
                        Text(
                            text = suggestion.cleanName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(suggestion) }
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownSection(
    state: CalculatorState,
    ratePerMile: Double,
    ratePerHour: Double,
    onCopy: (String) -> Unit
) {
    val rawMiles = parseCleanDouble(state.baseMiles) ?: 0.0
    val miles = roundUpToHalf(rawMiles)
    val rawHours = parseCleanDouble(state.baseHours) ?: 0.0
    val hours = roundUpToHalf(rawHours)
    
    val roundtripMiles = miles * 2
    val billableMiles = kotlin.math.max(0.0, roundtripMiles - 30.0)
    val billableHours = hours * 2
    
    val totalMileCost = billableMiles * ratePerMile
    val totalHourCost = billableHours * ratePerHour
    
    var totalAdditional = 0.0
    val validExpenses = state.additionalExpenses.mapNotNull { 
        val c = parseCleanDouble(it.cost)
        if (c != null && c > 0) {
            totalAdditional += c
            val n = it.name.trim().ifEmpty { "Unnamed Expense" }
            n to c
        } else null
    }
    
    val grandTotal = totalMileCost + totalHourCost + totalAdditional
    
    val hasData = miles > 0 || hours > 0 || totalAdditional > 0
    
    val breakdownText = buildString {
        appendLine("Mileage Cost: $%.2f".format(totalMileCost))
        appendLine("%.1f base miles × 2 (Roundtrip) = %.1f roundtrip miles".format(miles, roundtripMiles))
        appendLine("%.1f roundtrip miles - 30.0 deducted = %.1f billable miles".format(roundtripMiles, billableMiles))
        appendLine("%.1f miles × $%.3f = $%.2f".format(billableMiles, ratePerMile, totalMileCost))
        appendLine()
        
        appendLine("Hourly Cost: $%.2f".format(totalHourCost))
        appendLine("%.1f base hours × 2 (Roundtrip) = %.1f billable hours".format(hours, billableHours))
        appendLine("%.1f hours × $%.2f = $%.2f".format(billableHours, ratePerHour, totalHourCost))
        
        if (validExpenses.isNotEmpty()) {
            appendLine()
            appendLine("Additional Expenses: $%.2f".format(totalAdditional))
            validExpenses.forEach { (name, cost) ->
                appendLine("$name: $%.2f".format(cost))
            }
        }
        
        appendLine()
        appendLine("Grand Total: $%.2f".format(grandTotal))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!hasData) {
                Text(
                    text = "Enter values above to see the math breakdown.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Mileage Cost: $%.2f".format(totalMileCost), fontWeight = FontWeight.Bold)
                Text(
                    text = "%.1f base miles × 2 (Roundtrip) = %.1f roundtrip miles\n%.1f roundtrip miles - 30.0 deducted = %.1f billable miles\n%.1f miles × $%.3f = $%.2f".format(miles, roundtripMiles, roundtripMiles, billableMiles, billableMiles, ratePerMile, totalMileCost),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Text("Hourly Cost: $%.2f".format(totalHourCost), fontWeight = FontWeight.Bold)
                Text(
                    text = "%.1f base hours × 2 (Roundtrip) = %.1f billable hours\n%.1f hours × $%.2f = $%.2f".format(hours, billableHours, billableHours, ratePerHour, totalHourCost),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (validExpenses.isNotEmpty()) {
                    Text("Additional Expenses: $%.2f".format(totalAdditional), fontWeight = FontWeight.Bold)
                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                        validExpenses.forEach { (name, cost) ->
                            Text(
                                text = "$name: $%.2f".format(cost),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    text = "Grand Total: $%.2f".format(grandTotal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onCopy(breakdownText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Copy to Clipboard")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    viewModel: CostCalculatorViewModel,
    onBack: () -> Unit
) {
    val currentTheme by settingsManager.themeMode.collectAsState()
    val defaultAddress by settingsManager.defaultStartAddress.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var addressQuery by remember { mutableStateOf(defaultAddress) }
    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

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
                            searchJob = CoroutineScope(Dispatchers.Main).launch {
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
