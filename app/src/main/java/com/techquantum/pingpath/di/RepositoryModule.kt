package com.techquantum.pingpath.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.techquantum.pingpath.model.interfaces.EtaRepository
import com.techquantum.pingpath.model.interfaces.LocationRepository
import com.techquantum.pingpath.model.interfaces.SearchRepository
import com.techquantum.pingpath.repository.EtaRepositoryImpl
import com.techquantum.pingpath.repository.LocationRepositoryImpl
import com.techquantum.pingpath.repository.SearchRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    abstract fun bindEtaRepository(
        etaRepositoryImpl: EtaRepositoryImpl
    ): EtaRepository

    @Binds
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    abstract fun bindAlertRepository(
        alertRepositoryImpl: com.techquantum.pingpath.repository.AlertRepositoryImpl
    ): com.techquantum.pingpath.repository.AlertRepository
}
