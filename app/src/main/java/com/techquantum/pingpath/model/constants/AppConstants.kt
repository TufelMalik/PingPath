package com.techquantum.pingpath.model.constants

object AppConstants {
    const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    const val OSRM_BASE_URL = "https://router.project-osrm.org/"
    const val USER_AGENT = "ProximAlert/1.0"

    const val LOCATION_UPDATE_INTERVAL = 15000L // 15 seconds
    const val LOCATION_FASTEST_INTERVAL = 10000L // 10 seconds

    const val OSRM_POLL_INTERVAL_SECONDS = 30L
}
