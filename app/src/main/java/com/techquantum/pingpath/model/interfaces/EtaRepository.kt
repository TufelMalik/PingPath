package com.techquantum.pingpath.model.interfaces

import com.techquantum.pingpath.model.data.EtaResult

interface EtaRepository {
    suspend fun getEta(startLat: Double, startLon: Double, destLat: Double, destLon: Double): EtaResult?
}
