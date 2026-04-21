package com.techquantum.pingpath

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PingPathApp : Application() {
    companion object {
        lateinit var instance: PingPathApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
