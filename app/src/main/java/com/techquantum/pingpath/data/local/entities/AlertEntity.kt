package com.techquantum.pingpath.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val destinationName: String,
    val destinationAddress: String,
    val destinationLat: Double,
    val destinationLon: Double,
    val alarmLocationName: String,
    val alarmLocationAddress: String,
    val alarmLat: Double,
    val alarmLon: Double,
    val triggerRadiusKm: Double,
    val alarmSound: String,
    val isVibrationEnabled: Boolean,
    val status: String, // ACTIVE, COMPLETED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)
