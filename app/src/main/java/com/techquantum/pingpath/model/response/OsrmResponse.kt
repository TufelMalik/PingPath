package com.techquantum.pingpath.model.response

import com.google.gson.annotations.SerializedName

data class OsrmResponse(
    @SerializedName("code") val code: String,
    @SerializedName("routes") val routes: List<OsrmRoute>
)

data class OsrmRoute(
    @SerializedName("duration") val duration: Double,
    @SerializedName("distance") val distance: Double
)
