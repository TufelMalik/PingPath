package com.techquantum.pingpath.data.local.dao

import androidx.room.*
import com.techquantum.pingpath.data.local.entities.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Update
    suspend fun updateAlert(alert: AlertEntity): Int

    @Query("UPDATE alerts SET status = :status WHERE id = :alertId")
    suspend fun updateAlertStatus(alertId: String, status: String): Int

    @Delete
    suspend fun deleteAlert(alert: AlertEntity): Int

    @Query("SELECT * FROM alerts WHERE id = :alertId")
    suspend fun getAlertById(alertId: String): AlertEntity?
}
