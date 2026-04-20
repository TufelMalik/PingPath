package com.techquantum.pingpath

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PingPathApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
