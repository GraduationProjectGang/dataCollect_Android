package com.example.datacollect_android.etc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.datacollect_android.R
import com.example.datacollect_android.activity.u_key
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat

class DataCollectWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {
    //reference doc link
    //https://developer.android.com/topic/libraries/architecture/workmanager/advanced/coroutineworker
    //https://developer.android.com/training/location/request-updates


    //rotate vector variable
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference
    var mTimestamp:Long = 0
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    lateinit var mChannel : NotificationChannel

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork():  Result {
        val progress = "데이터 전송 중"
        createForegroundInfo(progress)

//        if (Build.VERSION.SDK_INT >= 28) {
//            val usageStatsManager =
//                applicationContext.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager?
//            if (usageStatsManager != null) {
//                Log.d(
//                    TAG,
//                    "getAppStandbyBucket():" + usageStatsManager.appStandbyBucket
//                )
//            }
//        }
        val intent = Intent(applicationContext, WakefulIntentService::class.java)
        Log.d("wakeful", "doWork")

        WakefulIntentService().enqueueWork(applicationContext,intent)

        return Result.success()
    }

    private fun createForegroundInfo(progress:String): ForegroundInfo {


        val CHANNEL_ID = "$applicationContext.packageName-${R.string.app_name}"
        val title = "사용자 데이터 수집"
        // This PendingIntent can be used to cancel the worker

        Log.d("setForeground","started")
        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_LOW)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.empty_swipe)
            .setOngoing(true)
            .build()
        Log.d("setForeground","build")

        return ForegroundInfo(100,notification)
    }

    fun createChannel(CHANNEL_ID:String, name:String, importance:Int) {

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = "데이터 전송 중"
        notificationManager.createNotificationChannel(mChannel)
        Log.d("setForeground","created")
    }
}