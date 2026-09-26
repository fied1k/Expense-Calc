package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CostCalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
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
                        Spacer(modifier = Modifier.height(12.dp))

                        AutocompleteField(
                            value = state.startQuery,
                            onValueChange = { viewModel.updateStartQuery(it) },
                            placeholder = "Start (e.g., Overland Park, KS)",
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
