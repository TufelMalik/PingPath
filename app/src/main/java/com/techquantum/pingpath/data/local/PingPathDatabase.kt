package com.techquantum.pingpath.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.techquantum.pingpath.data.local.dao.AlertDao
import com.techquantum.pingpath.data.local.dao.RecentLocationDao
import com.techquantum.pingpath.data.local.entities.AlertEntity
import com.techquantum.pingpath.data.local.entities.RecentLocationEntity

@Database(
    entities = [AlertEntity::class, RecentLocationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class PingPathDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun recentLocationDao(): RecentLocationDao

    companion object {
        const val DATABASE_NAME = "pingpath_db"
    }
}
