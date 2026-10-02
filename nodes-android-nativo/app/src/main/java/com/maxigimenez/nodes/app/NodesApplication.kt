package com.maxigimenez.nodes.app

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.maxigimenez.nodes.core.util.NotificationHelper
import com.maxigimenez.nodes.data.local.SettingsManager

class NodesApplication : Application(), DefaultLifecycleObserver {
    lateinit var settingsManager: SettingsManager

    companion object {
        var isAppInForeground = false
            private set
    }

    override fun onCreate() {
        settingsManager = SettingsManager(applicationContext)
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInForeground = false
    }
}
