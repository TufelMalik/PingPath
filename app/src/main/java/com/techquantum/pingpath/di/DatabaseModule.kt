package com.techquantum.pingpath.di

import android.content.Context
import androidx.room.Room
import com.techquantum.pingpath.data.local.PingPathDatabase
import com.techquantum.pingpath.data.local.dao.AlertDao
import com.techquantum.pingpath.data.local.dao.RecentLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PingPathDatabase {
        return Room.databaseBuilder(
            context,
            PingPathDatabase::class.java,
            PingPathDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAlertDao(database: PingPathDatabase): AlertDao {
        return database.alertDao()
    }

    @Provides
    @Singleton
    fun provideRecentLocationDao(database: PingPathDatabase): RecentLocationDao {
        return database.recentLocationDao()
    }
}
