package com.techquantum.pingpath.model.data

data class Destination(
    val lat: Double,
    val lon: Double,
    val displayName: String,
    val addressDetails: String? = null
)
