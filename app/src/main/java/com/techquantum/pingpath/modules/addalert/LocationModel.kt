package com.techquantum.pingpath.modules.addalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techquantum.pingpath.data.local.entities.AlertEntity
import com.techquantum.pingpath.model.data.HardwareSettingsModel
import com.techquantum.pingpath.model.data.LocationModel
import com.techquantum.pingpath.model.data.TimeOptionModel
import com.techquantum.pingpath.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.techquantum.pingpath.data.local.entities.RecentLocationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Clean Architecture Use Cases
class SearchLocationsUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(query: String, isDestination: Boolean): List<LocationModel> {
        return if (isDestination) repository.searchDestinations(query)
        else repository.searchAlarmLocations(query)
    }
}

class GetTimeOptionsUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(): List<TimeOptionModel> = repository.getTimeOptions()
}

data class AlertUIState(
    val currentStep: Int = 1,
    val destinationQuery: String = "",
    val destinationResults: List<LocationModel> = emptyList(),
    val selectedDestination: LocationModel? = null,
    
    val alarmQuery: String = "",
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

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val getTimeOptionsUseCase: GetTimeOptionsUseCase,
    private val repository: AlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlertUIState())
    val state: StateFlow<AlertUIState> = _state.asStateFlow()

    init {
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
                saveToRecent(event.location)
            }
            is AlertUIEvent.OnAlarmQueryChange -> {
                _state.update { it.copy(alarmQuery = event.query) }
                performSearch(event.query, isDestination = false)
            }
            is AlertUIEvent.OnAlarmSelected -> {
                _state.update { it.copy(selectedAlarmLocation = event.location, currentStep = 3) }
                saveToRecent(event.location)
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
                saveAlert()
            }
            is AlertUIEvent.OnMapClicked -> {
                viewModelScope.launch {
                    try {
                        val result = repository.reverseGeocode(event.lat, event.lon)
                        val location = LocationModel(
                            id = "map_${System.currentTimeMillis()}",
                            name = result?.name ?: "Selected Location",
                            address = result?.address ?: "Unknown Address",
                            latitude = event.lat,
                            longitude = event.lon
                        )
                        if (event.isDestination) {
                            _state.update { it.copy(selectedDestination = location, currentStep = 2) }
                            saveToRecent(location)
                        } else {
                            _state.update { it.copy(selectedAlarmLocation = location, currentStep = 3) }
                            saveToRecent(location)
                        }
                    } catch (e: Exception) {
                        // Fallback if geocoding fails
                        val fallbackLocation = LocationModel(
                            id = "map_${System.currentTimeMillis()}",
                            name = "Pinned Location",
                            address = "${String.format("%.4f", event.lat)}, ${String.format("%.4f", event.lon)}",
                            latitude = event.lat,
                            longitude = event.lon
                        )
                        if (event.isDestination) {
                            _state.update { it.copy(selectedDestination = fallbackLocation, currentStep = 2) }
                        } else {
                            _state.update { it.copy(selectedAlarmLocation = fallbackLocation, currentStep = 3) }
                        }
                    }
                }
            }
        }
    }

    private fun saveToRecent(location: LocationModel) {
        viewModelScope.launch {
            repository.insertRecentLocation(
                RecentLocationEntity(
                    name = location.name,
                    address = location.address,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        }
    }

    private fun saveAlert() {
        val currentState = _state.value
        val dest = currentState.selectedDestination ?: return
        val alarm = currentState.selectedAlarmLocation ?: return
        
        val newAlert = AlertEntity(
            id = UUID.randomUUID().toString(),
            destinationName = dest.name,
            destinationAddress = dest.address,
            destinationLat = dest.latitude,
            destinationLon = dest.longitude,
            alarmLocationName = alarm.name,
            alarmLocationAddress = alarm.address,
            alarmLat = alarm.latitude,
            alarmLon = alarm.longitude,
            triggerRadiusKm = 1.0, 
            alarmSound = currentState.hardwareSettings?.sound ?: "Default",
            isVibrationEnabled = true,
            status = "ACTIVE"
        )
        
        viewModelScope.launch {
            repository.insertAlert(newAlert)
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    private fun performSearch(query: String, isDestination: Boolean) {
        searchJob?.cancel()
        if (query.length < 3) return
        
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            val results = searchLocationsUseCase(query, isDestination)
            if (isDestination) {
                _state.update { it.copy(destinationResults = results) }
            } else {
                _state.update { it.copy(alarmResults = results) }
            }
        }
    }
}