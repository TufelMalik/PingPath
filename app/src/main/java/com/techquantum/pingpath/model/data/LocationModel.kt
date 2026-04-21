package com.techquantum.pingpath.model.data

data class LocationModel(
    val id: String,
    val name: String,
    val address: String,
    val type: LocationType = LocationType.TRAIN,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class LocationType { TRAIN, SUBWAY }
