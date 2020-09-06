package com.example.datacollect_android.etc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.legacy.content.WakefulBroadcastReceiver
import androidx.work.ForegroundInfo
import com.example.datacollect_android.R
import com.example.datacollect_android.activity.u_key
import com.example.datacollect_android.data_class.Locate
import com.example.datacollect_android.data_class.RotateVector
import com.example.datacollect_android.data_class.UsageStat
import com.example.datacollect_android.data_class.UsageStatsCollection
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class WakefulIntentService() : JobIntentService() {
    val TAG_LOCATION = "LocationTest"
    val TAG_ROTATE = "rotateVectorTest"
    val TAG_COROUTINE = "coroutineWorkerTest"
    val TAG_USAGE = "usageTest"
    val userKey = u_key


    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference
    var mTimestamp:Long = 0
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    val context = this
    val mySensorListener = WakefulSensorEventListener(context)



    companion object{
        val JOB_ID = 1001

    }

    fun enqueueWork(context: Context, intent: Intent){
        Log.d("wakeful","enqueueue")
        enqueueWork(context, WakefulIntentService::class.java, JOB_ID, intent)
    }


    override fun onHandleWork(intent: Intent) {

        if (Build.VERSION.SDK_INT >= 28) {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?
            if (usageStatsManager != null) {
                Log.d(
                    ContentValues.TAG,
                    "getAppStandbyBucket():" + usageStatsManager.appStandbyBucket
                )
            }
        }

        Log.d("wakeful", "onHandleHandle")
        mTimestamp = System.currentTimeMillis()//공통으로 쓰일 timestamp

        val iterationRange = 60
        val mutableListOrientationAngles = mutableListOf<String>()
        val stats = showAppUsageStats(getAppUsageStats(mTimestamp - 900000))

        //debug
//            printCallStack()


        val locationList = mySensorListener.initLocationParms()
        mySensorListener.startLocationUpdates()
        for (i in 1 .. iterationRange){
            //Repeat every 1s
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
            }
            mutableListOrientationAngles.add(mySensorListener.measureRotateVector())
            Log.d(TAG_COROUTINE, LocalDateTime.now().toString())
        }
        //stop location request when iteration was ended
        mySensorListener.stopLocationUpdates()

        var loc = Locate(mutableListOf(), dateFormat.format(mTimestamp))
        loc.locationList = locationList
        var usage = UsageStatsCollection(ArrayList(), "coroutine", mTimestamp, dateFormat.format(mTimestamp))
        usage.statsList = stats
        var rVector = RotateVector(mutableListOf(), dateFormat.format(mTimestamp))
        rVector.angleList = mutableListOrientationAngles

        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference
        dbReference.child("user").child(userKey).child("rotatevector").push().setValue(rVector)
        dbReference.child("user").child(userKey).child("usagestatsCoroutine").push().setValue(usage)
        dbReference.child("user").child(userKey).child("location").push().setValue(loc)
        dbReference.child("user").child(userKey).child("isRunning").setValue("true")

        if (isStopped) {
            dbReference.child("user").child(userKey).child("isRunning").setValue("false")
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent)
    }


    fun getAppUsageStats(time:Long): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -1)//1분간의 stats 파악
        Log.d("calcal",cal.toString())

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,time, mTimestamp
        )
        Log.d("appusing", queryUsageStats.size.toString())
        return queryUsageStats
    }
    fun showAppUsageStats(usageStats: MutableList<UsageStats>) : ArrayList<UsageStat> {

        val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
        Log.d("appusing", usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })
        var statsArr = ArrayList<UsageStat>()

        usageStats.forEach {
            if(it.totalTimeInForeground>0 && it.lastTimeUsed>mTimestamp-900000){
                statsArr.add(UsageStat(it.packageName, dateFormat.format(it.lastTimeUsed), it.totalTimeInForeground))
                Log.d("appusing",statsArr.last().toString())
            }
        }
        Log.d("appusing","statsArrLen: ${statsArr.size}")

        return statsArr
    }

    }
