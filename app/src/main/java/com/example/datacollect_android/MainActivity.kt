package com.example.datacollect_android

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*




class MainActivity : AppCompatActivity() {

    val INTERNET_REQUEST = 1234
    val permissionArr = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.PACKAGE_USAGE_STATS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
//    lateinit var dataCollectThread: Runnable
    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        notifySurvey()//설문 알림
        Log.d("rearea",this.toString())

        //TODO:적절한 시간에 설문 알림
        var result = getAppUsageStats()
        showAppUsageStats(result)
        Log.d("appusing","finished")
        getMotionData()
        Log.d("appusing","motiondata")
        initLocation()
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

    }
    fun initLocation() {
        var TAG = "applocation"

        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if(location == null) {
                    Log.e(TAG, "location get fail")
                } else {
                    Log.d(TAG, "${location.latitude} , ${location.longitude}")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "location error is ${it.message}")
                it.printStackTrace()
            }
    }

    fun getMotionData(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)



//        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)

//        val triggerEventListener = object : TriggerEventListener() {
//            override fun onTrigger(event: TriggerEvent?) {
//                Log.d("appusing","significant")
//                Toast.makeText(applicationContext,"significant motion",Toast.LENGTH_SHORT).show()
//            }
//        }

        var sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {

                when(event!!.sensor.type){
                     Sensor.TYPE_ROTATION_VECTOR ->{
                                val str = ("방향센서값 "
                                        + "방위각: " + event.values[0]
                                        + "피치 : " + event.values[1]
                                        + "롤 : " + event.values[2])
                        Log.d("sensorchange",str)
                         sensor_x.text = event.values[2].toString()
                         sensor_y.text = event.values[1].toString()
                         sensor_z.text = event.values[0].toString()
                    }
                    Sensor.TYPE_ACCELEROMETER ->{
                        val str = ("방향센서값 "
                                + "방위각: " + event.values[0]
                                + "피치 : " + event.values[1]
                                + "롤 : " + event.values[2])
                        Log.d("sensorchangeacc",str)
                        asensor_x.text = event.values[2].toString()
                        asensor_y.text = event.values[1].toString()
                        asensor_z.text = event.values[0].toString()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d("stracc",accuracy.toString())
            }
        }
        sensorManager.registerListener(sensorEventListener,gSensor,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener,aSensor,SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun getAppUsageStats(): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)    // 1

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        Log.d("appusing",usageStatsManager.toString())

        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis()
        )
        Log.d("appusing",queryUsageStats.size.toString())
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
    }
