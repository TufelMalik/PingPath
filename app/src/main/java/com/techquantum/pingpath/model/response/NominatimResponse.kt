package com.techquantum.pingpath.model.response

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("place_id") val placeId: Long,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("address") val address: Map<String, String>?
)
