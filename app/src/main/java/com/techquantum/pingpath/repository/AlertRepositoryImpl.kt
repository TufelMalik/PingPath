package com.techquantum.pingpath.repository

import com.techquantum.pingpath.data.local.dao.AlertDao
import com.techquantum.pingpath.data.local.dao.RecentLocationDao
import com.techquantum.pingpath.data.local.entities.AlertEntity
import com.techquantum.pingpath.data.local.entities.RecentLocationEntity
import com.techquantum.pingpath.model.data.HardwareSettingsModel
import com.techquantum.pingpath.model.data.LocationModel
import com.techquantum.pingpath.model.data.TimeOptionModel
import com.techquantum.pingpath.network.NominatimService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val alertDao: AlertDao,
    private val recentLocationDao: RecentLocationDao,
    private val nominatimService: NominatimService
) : AlertRepository {

    override fun getActiveAlerts(): Flow<List<AlertEntity>> = alertDao.getActiveAlerts()

    override fun getAllAlerts(): Flow<List<AlertEntity>> = alertDao.getAllAlerts()

    override suspend fun insertAlert(alert: AlertEntity): Long = alertDao.insertAlert(alert)

    override suspend fun updateAlert(alert: AlertEntity): Int = alertDao.updateAlert(alert)

    override suspend fun updateAlertStatus(alertId: String, status: String): Int = alertDao.updateAlertStatus(alertId, status)

    override suspend fun deleteAlert(alertId: String): Int {
        val alert = alertDao.getAlertById(alertId)
        return if (alert != null) {
            alertDao.deleteAlert(alert)
        } else 0
    }

    override suspend fun getAlertById(alertId: String): AlertEntity? = alertDao.getAlertById(alertId)

    override fun getRecentLocations(): Flow<List<RecentLocationEntity>> = recentLocationDao.getRecentLocations()

    override suspend fun insertRecentLocation(location: RecentLocationEntity): Long {
        val id = recentLocationDao.insertLocation(location)
        recentLocationDao.clearOldLocations()
        return id
    }

    override suspend fun searchDestinations(query: String): List<LocationModel> {
        return try {
            val response = nominatimService.search(query)
            response.map {
                LocationModel(
                    id = it.placeId.toString(),
                    name = it.displayName.split(",").firstOrNull() ?: "Unknown",
                    address = it.displayName,
                    latitude = it.lat.toDoubleOrNull() ?: 0.0,
                    longitude = it.lon.toDoubleOrNull() ?: 0.0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchAlarmLocations(query: String): List<LocationModel> {
        return searchDestinations(query)
    }

    override suspend fun reverseGeocode(lat: Double, lon: Double): LocationModel? {
        return try {
            val response = nominatimService.reverseGeocode(lat, lon)
            LocationModel(
                id = response.placeId.toString(),
                name = response.displayName.split(",").firstOrNull() ?: "Map Location",
                address = response.displayName,
                latitude = response.lat.toDoubleOrNull() ?: 0.0,
                longitude = response.lon.toDoubleOrNull() ?: 0.0
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTimeOptions(): List<TimeOptionModel> {
        return listOf(
            TimeOptionModel("t1", "5 minutes before", 5),
            TimeOptionModel("t2", "10 minutes before", 10),
            TimeOptionModel("t3", "15 minutes before", 15),
            TimeOptionModel("t4", "20 minutes before", 20),
            TimeOptionModel("t5", "30 minutes before", 30)
        )
    }

    override suspend fun getHardwareSettings(): HardwareSettingsModel {
        return HardwareSettingsModel("Default", "Pulse", "5 min")
    }
}
