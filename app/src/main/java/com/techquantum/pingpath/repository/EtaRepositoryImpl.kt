package com.techquantum.pingpath.repository

import com.techquantum.pingpath.model.data.EtaResult
import com.techquantum.pingpath.model.interfaces.EtaRepository
import com.techquantum.pingpath.network.OsrmService
import javax.inject.Inject

class EtaRepositoryImpl @Inject constructor(
    private val osrmService: OsrmService
) : EtaRepository {
    override suspend fun getEta(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): EtaResult? {
        return try {
            val coordinates = "$startLon,$startLat;$destLon,$destLat"
            val response = osrmService.getRoute(coordinates)
            if (response.code == "Ok" && response.routes.isNotEmpty()) {
                val route = response.routes.first()
                EtaResult(
                    durationSeconds = route.duration,
                    distanceMetres = route.distance
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
