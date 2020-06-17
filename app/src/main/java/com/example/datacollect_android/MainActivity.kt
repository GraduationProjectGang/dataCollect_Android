package com.example.datacollect_android

import android.app.*
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
import android.provider.Settings
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Comparator
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    val MULTIPLE_REQUEST = 1234
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")

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
        initPermission()


    }



    fun init() {
        initAlarm()
        /////////ButtonListener
        usermainBtn.setOnClickListener {
            val intent = Intent(this, UserMainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
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


        //usagedata
        var result = getAppUsageStats(System.currentTimeMillis()-900000)
        //15분전부터 현재까지 or FB에서 최근 데이터 저장한 시간 이후의 데이터
        showAppUsageStats(result)
    }

    private fun initUsageStats() {
        val TAG = "usageStats"
        var granted = false
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        Log.d(TAG, "===== CheckPhoneState isRooting granted = " + granted);

        if (granted == false)
        {
            // 권한이 없을 경우 권한 요구 페이지 이동
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent)
        }


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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun initPermission() {
        var rejectedPermissionList = ArrayList<String>()

        for (permission in permissionArr) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                rejectedPermissionList.add(permission)
            }
        }
        if(rejectedPermissionList.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), MULTIPLE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_REQUEST -> {
                Toast.makeText(this, "권한 모두 승인됨", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getAppUsageStats(time:Long): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -1)//1분간의 stats 파악
        Log.d("calcal",cal.toString())

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,time, System.currentTimeMillis()
        )

        Log.d("appusing", queryUsageStats.size.toString())
        return queryUsageStats
    }

    fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        Log.d("appusing", usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })
        var statsArr = ArrayList<UsageStat>()

        usageStats.forEach {
            if(it.lastTimeUsed>0){
                statsArr.add(UsageStat(it.packageName,dateFormat.format(it.lastTimeUsed),it.totalTimeInForeground))
                Log.d("appusing",statsArr.last().toString())
            }
        }
        Log.d("appusing","statsArrLen: ${statsArr.size}")
    }

    private fun initCollectingData() {//init Periodic work

//        val constraints = Constraints.Builder()
//            .setRequiresCharging(true)
//            .build()
//
//        //15분 마다 반복
//        val collectRequest =
//            PeriodicWorkRequestBuilder<DataCollectWorker>(15, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .build()
//
//        //WorkManager에 enqueue
//        WorkManager.getInstance(applicationContext)
//            .enqueue(collectRequest)
    }

}
