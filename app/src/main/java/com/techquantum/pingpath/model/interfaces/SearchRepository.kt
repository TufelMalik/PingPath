package com.techquantum.pingpath.model.interfaces

import com.techquantum.pingpath.model.data.Destination

interface SearchRepository {
    suspend fun searchDestination(query: String): List<Destination>
}
