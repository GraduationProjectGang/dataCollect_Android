package com.example.datacollect_android

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if(intent.action == "android.intent.action.BOOT_COMPLETED"){
            val receiver = ComponentName(context, BootReceiver::class.java)

            context.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }





    }
}
