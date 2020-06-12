package com.example.datacollect_android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    val INTERNET_REQUEST = 1234
    var permissionArr = arrayOf(android.Manifest.permission.PACKAGE_USAGE_STATS)
//    lateinit var dataCollectThread: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        notifySurvey()//설문 알림
        Log.d("rearea",this.toString())

        //TODO:적절한 시간에 설문 알림

        var result = getAppUsageStats()
        showAppUsageStats(result)
        Log.d("using","finished")
    }

    fun init(){
        /////////ButtonListener
        tutorialBtn.setOnClickListener {
            val intent = Intent(this, Tutorial1Activity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        stressBtn.setOnClickListener{
            val intent = Intent(this, StressCollectActivity::class.java)
            startActivity(intent)
        }
        fbBtn.setOnClickListener {
            val intent = Intent(this, FBTestActivity::class.java)
            startActivity(intent)
        }
        initPermission()
        initCollectingData()
    }

    fun getAppUsageStats(): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)    // 1

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        Log.d("appusing",usageStatsManager.toString())

        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis()
        )

        return queryUsageStats
    }

    fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        Log.d("appusing",usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })

        usageStats.forEach {
            Log.d("appusing", "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, " +
                    "totalTimeInForeground: ${it.totalTimeInForeground}")
        }
    }


    fun getMotionData(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        val triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                Toast.makeText(applicationContext,"significant motion",Toast.LENGTH_SHORT).show()
            }
        }
        mSensor?.also { sensor ->
            sensorManager.requestTriggerSensor(triggerEventListener, sensor)
        }
    }



    fun notifySurvey(){
        val CHANNEL_ID = "$packageName-${getString(R.string.app_name)}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }


        val intent = Intent(this, StressCollectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(baseContext, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)    // 3

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.full_swipe)
            setContentTitle(title)
            setContentText(getString(R.string.channel_description))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentIntent(pendingIntent)
        }
        with(NotificationManagerCompat.from(this)) {
            notify(200, builder.build())
        }
    }

    fun askPermission(requestPermission: Array<String>, REQ_PERMISSION: Int) {
        ActivityCompat.requestPermissions(this, requestPermission, REQ_PERMISSION)
    }

    fun checkAppPermission(request: Array<String>): Boolean { //앞으로 많이 사용하게 될 함수임
        val requestResult = BooleanArray(request.size)
        for (i in requestResult.indices) {
            requestResult[i] = ContextCompat.checkSelfPermission(this, request[i]) == PackageManager.PERMISSION_GRANTED
            if (!requestResult[i]) {
                Toast.makeText(
                    this,
                    "Failed to retrieve app usage statistics. " +
                            "You may need to enable access for this app through " +
                            "Settings > Security > Apps with usage access",
                    Toast.LENGTH_LONG
                ).show()
                //startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        return true
    }

    fun initPermission() {
        if(checkAppPermission(permissionArr)) {
            Toast.makeText(this, "권한 승인됨", Toast.LENGTH_SHORT).show()
        }
        else {
            askPermission(permissionArr, INTERNET_REQUEST)
        }
    }

    fun initCollectingData(){
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val collectRequest =
            PeriodicWorkRequestBuilder<DataCollectWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        
        WorkManager.getInstance(applicationContext)
            .enqueue(collectRequest)
    }
}
