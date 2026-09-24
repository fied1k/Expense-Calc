package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class Expense(val name: String, val cost: String)

data class Location(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

data class CalculatorState(
    val startQuery: String = "",
    val startLocation: Location? = null,
    val startAutocomplete: List<NominatimResult> = emptyList(),
    
    val endQuery: String = "",
    val endLocation: Location? = null,
    val endAutocomplete: List<NominatimResult> = emptyList(),
    
    val baseMiles: String = "",
    val baseHours: String = "",
    
    val additionalExpenses: List<Expense> = emptyList(),
    
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

fun parseCleanDouble(input: String?): Double? {
    if (input.isNullOrBlank()) return null
    val sanitized = input.replace("$", "").replace(",", "").trim()
    val parsed = sanitized.toDoubleOrNull() ?: return null
    return if (parsed < 0) 0.0 else parsed
}

fun roundUpToHalf(value: Double): Double {
    if (value <= 0.0) return 0.0
    val roundedVal = kotlin.math.round(value * 100000.0) / 100000.0
    return kotlin.math.ceil(roundedVal * 2.0) / 2.0
}

fun formatHalf(value: Double): String {
    val rounded = roundUpToHalf(value)
    if (rounded <= 0.0) return ""
    return if (rounded % 1.0 == 0.0) {
        "%.0f".format(rounded)
    } else {
        "%.1f".format(rounded)
    }
}

class CostCalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private var startSearchJob: Job? = null
    private var endSearchJob: Job? = null

    val RATE_PER_MILE = 0.725
    val RATE_PER_HOUR = 37.50
    val DRIVE_TIME_ADJUSTMENT = 0.85

    private var isSettingsInitialized = false

    fun initDefaultSettings(settingsManager: SettingsManager) {
        if (isSettingsInitialized) return
        isSettingsInitialized = true
        val defaultAddr = settingsManager.defaultStartAddress.value
        if (defaultAddr.isNotBlank() && _state.value.startQuery.isBlank()) {
            _state.update { it.copy(startQuery = defaultAddr) }
        }
    }

    fun applyDefaultStartAddress(address: String) {
        _state.update { it.copy(startQuery = address, startLocation = null) }
    }

    fun updateStartQuery(query: String) {
        _state.update { it.copy(startQuery = query, startLocation = null, errorMessage = null) }
        startSearchJob?.cancel()
        if (query.length < 3) {
            _state.update { it.copy(startAutocomplete = emptyList()) }
            return
        }
        startSearchJob = viewModelScope.launch {
            delay(500)
            try {
                val results = NetworkClient.nominatimApi.search(query)
                _state.update { it.copy(startAutocomplete = results) }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun updateEndQuery(query: String) {
        _state.update { it.copy(endQuery = query, endLocation = null, errorMessage = null) }
        endSearchJob?.cancel()
        if (query.length < 3) {
            _state.update { it.copy(endAutocomplete = emptyList()) }
            return
        }
        endSearchJob = viewModelScope.launch {
            delay(500)
            try {
                val results = NetworkClient.nominatimApi.search(query)
                _state.update { it.copy(endAutocomplete = results) }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun selectStartLocation(result: NominatimResult) {
        _state.update { 
            it.copy(
                startQuery = result.cleanName,
                startLocation = Location(result.cleanName, result.lat.toDoubleOrNull() ?: 0.0, result.lon.toDoubleOrNull() ?: 0.0),
                startAutocomplete = emptyList()
            ) 
        }
    }

    fun selectEndLocation(result: NominatimResult) {
        _state.update { 
            it.copy(
                endQuery = result.cleanName,
                endLocation = Location(result.cleanName, result.lat.toDoubleOrNull() ?: 0.0, result.lon.toDoubleOrNull() ?: 0.0),
                endAutocomplete = emptyList()
            ) 
        }
    }
    
    fun dismissAutocomplete() {
        _state.update { it.copy(startAutocomplete = emptyList(), endAutocomplete = emptyList()) }
    }

    fun calculateRoute() {
        if (_state.value.isLoading) return

        val current = _state.value
        val startQ = current.startQuery.trim()
        val endQ = current.endQuery.trim()
        
        if (startQ.isEmpty() || endQ.isEmpty()) {
            _state.update { it.copy(errorMessage = "Please enter both a start and end location.") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                val startCoords = getOrFetchLocation(startQ, current.startLocation)
                if (startCoords == null) {
                    throw Exception("Could not find location for starting address: '$startQ'. Please check spelling or select from suggestions.")
                }
                val endCoords = getOrFetchLocation(endQ, current.endLocation)
                if (endCoords == null) {
                    throw Exception("Could not find location for destination address: '$endQ'. Please check spelling or select from suggestions.")
                }

                // OSRM strictly requires lon,lat order
                val coordsString = "${"%.5f".format(startCoords.lon)},${"%.5f".format(startCoords.lat)};${"%.5f".format(endCoords.lon)},${"%.5f".format(endCoords.lat)}"
                
                val response = NetworkClient.osrmApi.getRoute(coordsString)
                if (response.code != "Ok" || response.routes.isNullOrEmpty()) {
                    throw Exception("Routing engine could not find a valid driving path between these locations.")
                }
                
                val route = response.routes[0]
                val miles = route.distance * 0.000621371
                val seconds = route.duration
                val rawHours = seconds / 3600.0
                val realisticHours = rawHours * DRIVE_TIME_ADJUSTMENT
                
                _state.update { 
                    it.copy(
                        baseMiles = formatHalf(miles),
                        baseHours = formatHalf(realisticHours),
                        isLoading = false
                    ) 
                }
            } catch (e: java.net.UnknownHostException) {
                _state.update { it.copy(isLoading = false, errorMessage = "Unable to connect. Please check your internet connection and try again.") }
            } catch (e: java.net.SocketTimeoutException) {
                _state.update { it.copy(isLoading = false, errorMessage = "Network request timed out. Please try again.") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "An unexpected error occurred while calculating the route.") }
            }
        }
    }

    private suspend fun getOrFetchLocation(query: String, existing: Location?): Location? {
        if (existing != null && existing.displayName == query) return existing
        val results = NetworkClient.nominatimApi.search(query, limit = 1)
        if (results.isEmpty()) return null
        val res = results[0]
        return Location(res.cleanName, res.lat.toDoubleOrNull() ?: 0.0, res.lon.toDoubleOrNull() ?: 0.0)
    }

    fun updateMiles(miles: String) {
        _state.update { it.copy(baseMiles = miles) }
    }

    fun formatMilesOnBlur() {
        val current = parseCleanDouble(_state.value.baseMiles)
        if (current != null && current > 0) {
            _state.update { it.copy(baseMiles = formatHalf(current)) }
        }
    }

    fun updateHours(hours: String) {
        _state.update { it.copy(baseHours = hours) }
    }

    fun formatHoursOnBlur() {
        val current = parseCleanDouble(_state.value.baseHours)
        if (current != null && current > 0) {
            _state.update { it.copy(baseHours = formatHalf(current)) }
        }
    }

    private var lastAddExpenseTime = 0L

    fun addExpense() {
        val now = System.currentTimeMillis()
        if (now - lastAddExpenseTime < 400) return

        val expenses = _state.value.additionalExpenses.toMutableList()
        if (expenses.isNotEmpty() && expenses.last().name.isBlank() && expenses.last().cost.isBlank()) {
            return
        }
        lastAddExpenseTime = now
        expenses.add(Expense("", ""))
        _state.update { it.copy(additionalExpenses = expenses) }
    }

    fun updateExpense(index: Int, name: String, cost: String) {
        val expenses = _state.value.additionalExpenses.toMutableList()
        if (index in expenses.indices) {
            expenses[index] = Expense(name, cost)
            _state.update { it.copy(additionalExpenses = expenses) }
        }
    }

    fun removeExpense(index: Int) {
        val expenses = _state.value.additionalExpenses.toMutableList()
        if (index in expenses.indices) {
            expenses.removeAt(index)
            _state.update { it.copy(additionalExpenses = expenses) }
        }
    }
}
