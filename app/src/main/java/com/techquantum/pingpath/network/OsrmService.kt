package com.techquantum.pingpath.network

import com.techquantum.pingpath.model.response.OsrmResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmService {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String, // lon,lat;lon,lat
        @Query("overview") overview: String = "false",
        @Query("annotations") annotations: String = "false"
    ): OsrmResponse
}
