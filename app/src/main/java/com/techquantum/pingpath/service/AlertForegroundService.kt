package com.techquantum.pingpath.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.techquantum.pingpath.model.constants.AppConstants
import com.techquantum.pingpath.model.enums.AlertMode
import com.techquantum.pingpath.model.interfaces.EtaRepository
import com.techquantum.pingpath.model.interfaces.LocationRepository
import com.techquantum.pingpath.utils.helpers.DistanceHelper
import com.techquantum.pingpath.utils.helpers.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AlertForegroundService : Service() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var etaRepository: EtaRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false
    private var lastEtaPollTime = 0L

    // In a full implementation, these would come from DataStore
    private var currentMode = AlertMode.PROXIMITY
    private var threshold = 1000 // 1000 metres or minutes
    private var destLat = 0.0
    private var destLon = 0.0
    private var destinationName = "Destination"

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoring()
            return START_NOT_STICKY
        }

        // Parse alert details from intent
        destLat = intent?.getDoubleExtra(EXTRA_DEST_LAT, 0.0) ?: 0.0
        destLon = intent?.getDoubleExtra(EXTRA_DEST_LON, 0.0) ?: 0.0
        destinationName = intent?.getStringExtra(EXTRA_DEST_NAME) ?: "Destination"
        val modeStr = intent?.getStringExtra(EXTRA_MODE) ?: AlertMode.PROXIMITY.name
        currentMode = AlertMode.valueOf(modeStr)
        threshold = intent?.getIntExtra(EXTRA_THRESHOLD, 1000) ?: 1000

        startForeground(
            NotificationHelper.NOTIFICATION_ID_SERVICE,
            NotificationHelper.buildServiceNotification(this, destinationName)
        )

        startLocationMonitoring()

        return START_STICKY
    }

    private fun startLocationMonitoring() {
        if (isMonitoring) return
        isMonitoring = true

        serviceScope.launch {
            locationRepository.getUserLocation().collectLatest { location ->
                Log.d("AlertService", "Location update: ${location.latitude}, ${location.longitude}")
                
                if (currentMode == AlertMode.PROXIMITY) {
                    val distance = DistanceHelper.calculateHaversineDistance(
                        location.latitude, location.longitude,
                        destLat, destLon
                    )
                    Log.d("AlertService", "Distance remaining: $distance")
                    
                    if (distance <= threshold) {
                        triggerAlarm()
                    }
                } else {
                    // ETA mode: poll every 30s
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastEtaPollTime >= AppConstants.OSRM_POLL_INTERVAL_SECONDS * 1000) {
                        lastEtaPollTime = currentTime
                        
                        val etaResult = etaRepository.getEta(
                            location.latitude, location.longitude,
                            destLat, destLon
                        )
                        
                        etaResult?.let {
                            Log.d("AlertService", "ETA remaining: ${it.durationSeconds}s")
                            if (it.durationSeconds <= threshold * 60) {
                                triggerAlarm()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun triggerAlarm() {
        NotificationHelper.fireAlarm(this, destinationName)
        stopMonitoring() // Once fired, stop monitoring.
    }

    private fun stopMonitoring() {
        isMonitoring = false
        serviceScope.coroutineContext.cancelChildren()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.techquantum.pingpath.START_ALERT"
        const val ACTION_STOP = "com.techquantum.pingpath.STOP_ALERT"

        const val EXTRA_DEST_LAT = "extra_dest_lat"
        const val EXTRA_DEST_LON = "extra_dest_lon"
        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_THRESHOLD = "extra_threshold"
    }
}
