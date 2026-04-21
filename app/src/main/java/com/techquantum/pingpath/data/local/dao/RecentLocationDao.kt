package com.techquantum.pingpath.data.local.dao

import androidx.room.*
import com.techquantum.pingpath.data.local.entities.RecentLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentLocationDao {
    @Query("SELECT * FROM recent_locations ORDER BY timestamp DESC LIMIT 20")
    fun getRecentLocations(): Flow<List<RecentLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: RecentLocationEntity): Long

    @Query("DELETE FROM recent_locations WHERE id NOT IN (SELECT id FROM recent_locations ORDER BY timestamp DESC LIMIT 20)")
    suspend fun clearOldLocations(): Int
}
