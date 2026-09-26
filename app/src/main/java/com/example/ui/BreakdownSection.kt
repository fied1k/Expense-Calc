package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CalculatorState
import com.example.parseCleanDouble
import com.example.roundUpToHalf
import java.util.Locale

@Composable
fun BreakdownSection(
    state: CalculatorState,
    ratePerMile: Double = state.ratePerMile,
    ratePerHour: Double = state.ratePerHour,
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

    val breakdownText = buildString {
        appendLine("Cost Breakdown:")
        if (state.startQuery.isNotBlank() && state.endQuery.isNotBlank()) {
            appendLine("Route: ${state.startQuery} to ${state.endQuery}")
        }
        appendLine("Mileage Cost: ${String.format(Locale.US, "$%.2f", totalMileCost)}")
        appendLine("${String.format(Locale.US, "%.1f", miles)} base miles × 2 (Roundtrip) = ${String.format(Locale.US, "%.1f", roundtripMiles)} roundtrip miles")
        appendLine("${String.format(Locale.US, "%.1f", roundtripMiles)} roundtrip miles - 30.0 deducted = ${String.format(Locale.US, "%.1f", billableMiles)} billable miles")
        appendLine("${String.format(Locale.US, "%.1f", billableMiles)} miles × ${String.format(Locale.US, "$%.3f", ratePerMile)} = ${String.format(Locale.US, "$%.2f", totalMileCost)}")
        appendLine()
        appendLine("Hourly Cost: ${String.format(Locale.US, "$%.2f", totalHourCost)}")
        appendLine("${String.format(Locale.US, "%.1f", hours)} base hours × 2 (Roundtrip) = ${String.format(Locale.US, "%.1f", billableHours)} billable hours")
        appendLine("${String.format(Locale.US, "%.1f", billableHours)} hours × ${String.format(Locale.US, "$%.2f", ratePerHour)} = ${String.format(Locale.US, "$%.2f", totalHourCost)}")
        if (validExpenses.isNotEmpty()) {
            appendLine()
            appendLine("Additional Expenses: ${String.format(Locale.US, "$%.2f", totalAdditional)}")
            validExpenses.forEach { (name, cost) ->
                appendLine("$name: ${String.format(Locale.US, "$%.2f", cost)}")
            }
        }
        appendLine()
        appendLine("Grand Total: ${String.format(Locale.US, "$%.2f", grandTotal)}")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cost Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Mileage breakdown
            Text("Mileage Cost: ${String.format(Locale.US, "$%.2f", totalMileCost)}", fontWeight = FontWeight.Bold)
            Text(
                text = "${String.format(Locale.US, "%.1f", miles)} base miles × 2 (Roundtrip) = ${String.format(Locale.US, "%.1f", roundtripMiles)} roundtrip miles\n${String.format(Locale.US, "%.1f", roundtripMiles)} roundtrip miles - 30.0 deducted = ${String.format(Locale.US, "%.1f", billableMiles)} billable miles\n${String.format(Locale.US, "%.1f", billableMiles)} miles × ${String.format(Locale.US, "$%.3f", ratePerMile)} = ${String.format(Locale.US, "$%.2f", totalMileCost)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hourly breakdown
            Text("Hourly Cost: ${String.format(Locale.US, "$%.2f", totalHourCost)}", fontWeight = FontWeight.Bold)
            Text(
                text = "${String.format(Locale.US, "%.1f", hours)} base hours × 2 (Roundtrip) = ${String.format(Locale.US, "%.1f", billableHours)} billable hours\n${String.format(Locale.US, "%.1f", billableHours)} hours × ${String.format(Locale.US, "$%.2f", ratePerHour)} = ${String.format(Locale.US, "$%.2f", totalHourCost)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (validExpenses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Additional Expenses: ${String.format(Locale.US, "$%.2f", totalAdditional)}", fontWeight = FontWeight.Bold)
                Column(modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                    validExpenses.forEach { (name, cost) ->
                        Text(
                            text = "$name: ${String.format(Locale.US, "$%.2f", cost)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Grand Total: ${String.format(Locale.US, "$%.2f", grandTotal)}",
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
