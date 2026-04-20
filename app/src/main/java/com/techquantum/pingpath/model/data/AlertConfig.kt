package com.techquantum.pingpath.model.data

import com.techquantum.pingpath.model.enums.AlertMode

data class AlertConfig(
    val destination: Destination,
    val mode: AlertMode,
    val thresholdValue: Int, // Metres if PROXIMITY, Minutes if ETA
    val isActive: Boolean = false
)
