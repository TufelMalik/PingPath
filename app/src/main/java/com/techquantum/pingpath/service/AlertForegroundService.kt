package com.techquantum.pingpath.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.techquantum.pingpath.model.interfaces.EtaRepository
import com.techquantum.pingpath.model.interfaces.LocationRepository
import com.techquantum.pingpath.utils.helpers.DistanceHelper
import com.techquantum.pingpath.utils.helpers.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.techquantum.pingpath.repository.AlertRepository

@AndroidEntryPoint
class AlertForegroundService : Service() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var alertRepository: AlertRepository
    @Inject lateinit var etaRepository: EtaRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false
    private var lastEtaPollTime = 0L

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoring()
            return START_NOT_STICKY
        }

        startForeground(
            NotificationHelper.NOTIFICATION_ID_SERVICE,
            NotificationHelper.buildServiceNotification(this, "Monitoring active alerts...")
        )

        startLocationMonitoring()

        return START_STICKY
    }

    private fun startLocationMonitoring() {
        if (isMonitoring) return
        isMonitoring = true

        serviceScope.launch {
            // Combine location updates with active alerts from DB
            locationRepository.getUserLocation().collectLatest { location ->
                val activeAlerts = alertRepository.getActiveAlerts().first()
                
                activeAlerts.forEach { alert ->
                    val distance = DistanceHelper.calculateHaversineDistance(
                        location.latitude, location.longitude,
                        alert.alarmLat, alert.alarmLon
                    )
                    
                    Log.d("AlertService", "Alert ${alert.id} - Distance remaining: $distance")
                    
                    // Threshold is 1km for now, or use a field from alert if added
                    if (distance <= 1000) { 
                        triggerAlarm(alert.id, alert.destinationName)
                    }
                }
                
                // If no more active alerts, stop service
                if (activeAlerts.isEmpty()) {
                    stopMonitoring()
                }
            }
        }
    }

    private fun triggerAlarm(alertId: String, destinationName: String) {
        NotificationHelper.fireAlarm(this, destinationName)
        
        serviceScope.launch {
            alertRepository.updateAlertStatus(alertId, "FIRED")
        }
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
    }
}
