package com.techquantum.pingpath.repository

import com.techquantum.pingpath.data.local.entities.AlertEntity
import com.techquantum.pingpath.data.local.entities.RecentLocationEntity
import com.techquantum.pingpath.model.data.HardwareSettingsModel
import com.techquantum.pingpath.model.data.LocationModel
import com.techquantum.pingpath.model.data.TimeOptionModel
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    // Alerts
    fun getActiveAlerts(): Flow<List<AlertEntity>>
    fun getAllAlerts(): Flow<List<AlertEntity>>
    suspend fun insertAlert(alert: AlertEntity): Long
    suspend fun updateAlert(alert: AlertEntity): Int
    suspend fun updateAlertStatus(alertId: String, status: String): Int
    suspend fun deleteAlert(alertId: String): Int
    suspend fun getAlertById(alertId: String): AlertEntity?

    // Recent Locations
    fun getRecentLocations(): Flow<List<RecentLocationEntity>>
    suspend fun insertRecentLocation(location: RecentLocationEntity): Long

    // Search & External API
    suspend fun searchDestinations(query: String): List<LocationModel>
    suspend fun searchAlarmLocations(query: String): List<LocationModel>
    suspend fun reverseGeocode(lat: Double, lon: Double): LocationModel?
    suspend fun getTimeOptions(): List<TimeOptionModel>
    suspend fun getHardwareSettings(): HardwareSettingsModel
}
