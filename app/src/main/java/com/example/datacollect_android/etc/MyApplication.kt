package com.example.datacollect_android.etc

import android.app.Application
import androidx.work.Configuration

class MyApplication : Application(), Configuration.Provider{
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}