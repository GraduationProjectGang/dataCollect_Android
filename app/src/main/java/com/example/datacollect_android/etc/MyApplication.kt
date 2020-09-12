package com.example.datacollect_android.etc

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import kotlin.coroutines.coroutineContext

class MyApplication : Application(), Configuration.Provider{
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}