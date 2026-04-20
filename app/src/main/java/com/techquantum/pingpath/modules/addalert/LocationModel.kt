package com.techquantum.pingpath.modules.addalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ==========================================
// 1. DOMAIN LAYER (Models & Interfaces)
// ==========================================

data class LocationModel(
    val id: String,
    val name: String,
    val address: String,
    val type: LocationType
)

enum class LocationType { TRAIN, SUBWAY }

data class TimeOptionModel(
    val id: String,
    val label: String,
    val minutes: Int
)

data class HardwareSettingsModel(
    val sound: String,
    val vibration: String,
    val snooze: String
)

interface AlertRepository {
    suspend fun searchDestinations(query: String): List<LocationModel>
    suspend fun searchAlarmLocations(query: String): List<LocationModel>
    suspend fun reverseGeocode(lat: Double, lon: Double): LocationModel?
    suspend fun getTimeOptions(): List<TimeOptionModel>
    suspend fun getHardwareSettings(): HardwareSettingsModel
}

// Clean Architecture Use Cases
class SearchLocationsUseCase(private val repository: AlertRepository) {
    suspend operator fun invoke(query: String, isDestination: Boolean): List<LocationModel> {
        return if (isDestination) repository.searchDestinations(query)
        else repository.searchAlarmLocations(query)
    }
}

class GetTimeOptionsUseCase(private val repository: AlertRepository) {
    suspend operator fun invoke(): List<TimeOptionModel> = repository.getTimeOptions()
}


// ==========================================

class AlertRepositoryImpl(private val context: Context) : AlertRepository {
    private val allDestinations = listOf(
        LocationModel("1", "Mumbai Central Railway Station", "Dr. A.B. Road, Mumbai, MH", LocationType.TRAIN),
        LocationModel("2", "Dadar Railway Station", "Dadar, Mumbai", LocationType.TRAIN),
        LocationModel("3", "Andheri Metro Station", "Andheri, Mumbai", LocationType.SUBWAY),
        LocationModel("4", "Bandra Terminus", "Bandra, Mumbai", LocationType.TRAIN)
    )

    private val allAlarmLocations = listOf(
        LocationModel("2", "Dadar Railway Station", "Mumbai, Maharashtra", LocationType.TRAIN),
        LocationModel("4", "Bandra Railway Station", "Mumbai, Maharashtra", LocationType.TRAIN),
        LocationModel("5", "Andheri Station", "Mumbai, Maharashtra", LocationType.TRAIN),
        LocationModel("6", "Borivali Station", "Mumbai, Maharashtra", LocationType.TRAIN)
    )

    private val timeOptions = listOf(
        TimeOptionModel("t1", "5 minutes before", 5),
        TimeOptionModel("t2", "10 minutes before", 10),
        TimeOptionModel("t3", "15 minutes before", 15),
        TimeOptionModel("t4", "20 minutes before", 20),
        TimeOptionModel("t5", "30 minutes before", 30)
    )

    override suspend fun searchDestinations(query: String): List<LocationModel> {
        return searchPlaces(query)
    }

    override suspend fun searchAlarmLocations(query: String): List<LocationModel> {
        return searchPlaces(query)
    }

    private suspend fun searchPlaces(query: String): List<LocationModel> = withContext(Dispatchers.IO) {
        if (query.isEmpty()) return@withContext emptyList()
        
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=5&countrycodes=in&accept-language=en")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "PingPathApp")
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                val results = mutableListOf<LocationModel>()
                
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val placeId = item.optString("place_id", java.util.UUID.randomUUID().toString())
                    
                    val displayNameStr = item.optString("display_name", "")
                    val parts = displayNameStr.split(",").map { it.trim() }
                    
                    val name = item.optString("name").takeIf { it.isNotEmpty() } ?: parts.firstOrNull() ?: "Unknown"
                    
                    // The address should be everything except the first part if it matches the name
                    val address = if (parts.size > 1 && parts[0] == name) {
                        parts.drop(1).joinToString(", ")
                    } else if (parts.size > 1) {
                        displayNameStr
                    } else {
                        ""
                    }
                    
                    results.add(
                        LocationModel(
                            id = placeId,
                            name = name,
                            address = address,
                            type = LocationType.TRAIN
                        )
                    )
                }
                return@withContext results
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "OSM Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
        return@withContext emptyList()
    }

    override suspend fun reverseGeocode(lat: Double, lon: Double): LocationModel? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "PingPathApp")
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val item = org.json.JSONObject(response)
                val placeId = item.optString("place_id", java.util.UUID.randomUUID().toString())
                val displayName = item.optString("display_name", "")
                val name = item.optString("name").takeIf { it.isNotEmpty() } ?: displayName.split(",").firstOrNull() ?: "Map Location"
                
                return@withContext LocationModel(
                    id = placeId,
                    name = name,
                    address = displayName,
                    type = LocationType.TRAIN
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    override suspend fun getTimeOptions(): List<TimeOptionModel> = timeOptions

    override suspend fun getHardwareSettings(): HardwareSettingsModel = 
        HardwareSettingsModel("Default", "Pulse", "5 min")
}

// ==========================================
// 3. PRESENTATION LAYER (State, Events, ViewModel)
// ==========================================

data class AlertUIState(
    val currentStep: Int = 1,
    val destinationQuery: String = "Mumbai",
    val destinationResults: List<LocationModel> = emptyList(),
    val selectedDestination: LocationModel? = null,
    
    val alarmQuery: String = "Dadar",
    val alarmResults: List<LocationModel> = emptyList(),
    val selectedAlarmLocation: LocationModel? = null,
    
    val timeOptions: List<TimeOptionModel> = emptyList(),
    val selectedTime: TimeOptionModel? = null,
    val isTimeDropdownOpen: Boolean = false,
    
    val hardwareSettings: HardwareSettingsModel? = null
)

sealed class AlertUIEvent {
    data class OnDestinationQueryChange(val query: String) : AlertUIEvent()
    data class OnDestinationSelected(val location: LocationModel) : AlertUIEvent()
    data class OnAlarmQueryChange(val query: String) : AlertUIEvent()
    data class OnAlarmSelected(val location: LocationModel) : AlertUIEvent()
    data class OnMapClicked(val lat: Double, val lon: Double, val isDestination: Boolean) : AlertUIEvent()
    object ToggleTimeDropdown : AlertUIEvent()
    data class OnTimeSelected(val time: TimeOptionModel) : AlertUIEvent()
    data class OnChangeStepRequested(val targetStep: Int) : AlertUIEvent()
    object OnSaveClicked : AlertUIEvent()
}

class AlertViewModel(
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val getTimeOptionsUseCase: GetTimeOptionsUseCase,
    private val repository: AlertRepository // For direct setting access
) : ViewModel() {

    private val _state = MutableStateFlow(AlertUIState())
    val state: StateFlow<AlertUIState> = _state.asStateFlow()

    init {
        // Load initial data based on default mock query
        onEvent(AlertUIEvent.OnDestinationQueryChange("Mumbai"))
        onEvent(AlertUIEvent.OnAlarmQueryChange("Dadar"))
        
        viewModelScope.launch {
            _state.update { it.copy(
                timeOptions = getTimeOptionsUseCase(),
                hardwareSettings = repository.getHardwareSettings()
            ) }
        }
    }

    fun onEvent(event: AlertUIEvent) {
        when (event) {
            is AlertUIEvent.OnDestinationQueryChange -> {
                _state.update { it.copy(destinationQuery = event.query) }
                performSearch(event.query, isDestination = true)
            }
            is AlertUIEvent.OnDestinationSelected -> {
                _state.update { it.copy(selectedDestination = event.location, currentStep = 2) }
            }
            is AlertUIEvent.OnAlarmQueryChange -> {
                _state.update { it.copy(alarmQuery = event.query) }
                performSearch(event.query, isDestination = false)
            }
            is AlertUIEvent.OnAlarmSelected -> {
                _state.update { it.copy(selectedAlarmLocation = event.location, currentStep = 3) }
            }
            is AlertUIEvent.ToggleTimeDropdown -> {
                _state.update { it.copy(isTimeDropdownOpen = !it.isTimeDropdownOpen) }
            }
            is AlertUIEvent.OnTimeSelected -> {
                _state.update { it.copy(
                    selectedTime = event.time, 
                    isTimeDropdownOpen = false,
                    currentStep = 4
                ) }
            }
            is AlertUIEvent.OnChangeStepRequested -> {
                _state.update { it.copy(currentStep = event.targetStep) }
            }
            is AlertUIEvent.OnSaveClicked -> {
                val currentState = _state.value
                val newAlert = com.techquantum.pingpath.modules.home.AlertEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    triggerType = "ON ENTRY",
                    destination = currentState.selectedDestination?.name ?: "Unknown",
                    contextText = "Alarm at ${currentState.selectedAlarmLocation?.name ?: "Unknown"}",
                    distanceRemaining = "N/A",
                    eta = "N/A",
                    progressPercent = 0.0f,
                    configSummary = "${currentState.selectedTime?.label ?: "N/A"} · Sound"
                )
                com.techquantum.pingpath.modules.home.InMemoryAlertsDatabase.addAlert(newAlert)
            }
            is AlertUIEvent.OnMapClicked -> {
                viewModelScope.launch {
                    val result = repository.reverseGeocode(event.lat, event.lon)
                    if (result != null) {
                        if (event.isDestination) {
                            _state.update { it.copy(selectedDestination = result, destinationQuery = result.name, currentStep = 2) }
                        } else {
                            _state.update { it.copy(selectedAlarmLocation = result, alarmQuery = result.name, currentStep = 3) }
                        }
                    }
                }
            }
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    private fun performSearch(query: String, isDestination: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce for 500ms to avoid rate limiting
            val results = searchLocationsUseCase(query, isDestination)
            if (isDestination) {
                _state.update { it.copy(destinationResults = results) }
            } else {
                _state.update { it.copy(alarmResults = results) }
            }
        }
    }
}