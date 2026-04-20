package com.techquantum.pingpath.model.interfaces

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getUserLocation(): Flow<Location>
}
