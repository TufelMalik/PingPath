package com.techquantum.pingpath.repository

import com.techquantum.pingpath.model.data.Destination
import com.techquantum.pingpath.model.interfaces.SearchRepository
import com.techquantum.pingpath.network.NominatimService
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val nominatimService: NominatimService
) : SearchRepository {
    override suspend fun searchDestination(query: String): List<Destination> {
        return try {
            val results = nominatimService.search(query = query)
            results.map {
                Destination(
                    lat = it.lat.toDoubleOrNull() ?: 0.0,
                    lon = it.lon.toDoubleOrNull() ?: 0.0,
                    displayName = it.displayName,
                    addressDetails = it.address?.toString()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
