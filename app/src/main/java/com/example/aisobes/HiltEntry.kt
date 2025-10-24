package com.example.aisobes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltEntry: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
