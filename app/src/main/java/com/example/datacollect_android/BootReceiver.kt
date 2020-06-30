package com.example.datacollect_android

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("bootReceiver","Bootreceived")
        setAlarmAt(context,10)

    }

    fun setAlarmAt(context:Context, time: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
        }
        Log.d("alarmset","alarmsetat${time}")
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("time",time)

        val alarmUp = PendingIntent.getBroadcast(context, 10, alarmIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        if (alarmUp) {
            Log.d("myTag", "Alarm is already active")
        }else{
            Log.d("myTag", "Alarm doesn't exist")
            val pendingIntent =
                PendingIntent.getBroadcast(context, 10, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        }

    }


}

