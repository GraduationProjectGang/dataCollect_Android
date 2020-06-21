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
import java.text.SimpleDateFormat
import java.util.*


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("bootReceiver","Bootreceived")
        setAlarmAt(context,10)
        setAlarmAt(context,14)
        setAlarmAt(context,18)
        setAlarmAt(context,22)
    }


    fun setAlarmAt(context:Context, time: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
        }

        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, time, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (alarmManager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                Log.d("alal","dd")
//

            //test
//            alarmManager.setAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP,
//                    System.currentTimeMillis()+6000,
//                    pendingIntent
//                )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}

