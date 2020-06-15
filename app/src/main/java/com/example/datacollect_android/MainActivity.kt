package com.example.datacollect_android

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Comparator


class MainActivity : AppCompatActivity() {


    val INTERNET_REQUEST = 1234
    var permissionArr = arrayOf(
        android.Manifest.permission.PACKAGE_USAGE_STATS,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    //    lateinit var dataCollectThread: Runnable
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //    lateinit var dataCollectThread: Runnable
    var alarmMgr: AlarmManager? = null
    lateinit var alarmIntent: PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

       // notifySurvey()//설문 알림
        Log.d("rearea", this.toString())

        //TODO:적절한 시간에 설문 알림
        var result = getAppUsageStats()
        showAppUsageStats(result)
        Log.d("using", "finished")
    }



    fun init() {
        initAlarm()
        /////////ButtonListener
        tutorialBtn.setOnClickListener {
            val intent = Intent(this, Tutorial1Activity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        stressBtn.setOnClickListener {
            val intent = Intent(this, StressCollectActivity::class.java)
            startActivity(intent)
        }
        loginBtn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        fbBtn.setOnClickListener {
            val intent = Intent(this, FBTestActivity::class.java)
            startActivity(intent)
        }

        initCollectingData()
        button2.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("firere", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.d("firere", token)
            })

    }

    fun initAlarm() {

        setAlarmAt(14)
        setAlarmAt(22)
        // Set the alarm to start at approximately 2p.m. and 10p.m.

        val pm: PackageManager = this.packageManager
        val receiver = ComponentName(this, BootReceiver::class.java)

        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        );

    }


    fun setAlarmAt(time: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
        }

        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, time, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

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


    fun getAppUsageStats(): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)    // 1

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        Log.d("appusing", usageStatsManager.toString())

        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis()
        )
        Log.d("appusing", queryUsageStats.size.toString())
        return queryUsageStats
    }

    fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        Log.d("appusing", usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })

        usageStats.forEach {
            Log.d(
                "appusing",
                "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, " +
                        "totalTimeInForeground: ${it.totalTimeInForeground}"
            )
        }
    }


    fun getMotionData() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        val triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                Toast.makeText(applicationContext, "significant motion", Toast.LENGTH_SHORT).show()
            }
        }
        mSensor?.also { sensor ->
            sensorManager.requestTriggerSensor(triggerEventListener, sensor)
        }
    }

    private fun initCollectingData() {//init Periodic work

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        //15분 마다 반복
        val collectRequest =
            PeriodicWorkRequestBuilder<DataCollectWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        //WorkManager에 enqueue
        WorkManager.getInstance(applicationContext)
            .enqueue(collectRequest)
    }

}
