package com.example.datacollect_android

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.datacollect_android.R.string.channel_description as channel_description1
import android.provider.Settings.System.getString
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import java.util.*

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val CHANNEL_ID = "$context.packageName-${R.string.app_name}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = R.string.channel_name
            val descriptionText = R.string.channel_description
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name.toString(), importance)
            mChannel.description = descriptionText.toString()
            val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val intent = Intent(context, StressCollectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.full_swipe)
            setContentTitle("스트레스 측정 설문 요청")
            setContentText("스트레스 설문에 참여해주세요\uD83D\uDD25")
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(true)
            setContentIntent(pendingIntent)
        }
        with(NotificationManagerCompat.from(context)) {
            notify(200, builder.build())
            Log.d("alal","notified")
        }
    }
}