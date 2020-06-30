package com.example.datacollect_android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings.System.getString
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemServiceName
import androidx.work.*
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class DataCollectWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), SensorEventListener {
    //reference doc link
    //https://developer.android.com/topic/libraries/architecture/workmanager/advanced/coroutineworker
    //https://developer.android.com/training/location/request-updates

    val TAG_LOCATION = "LocationTest"
    val TAG_ROTATE = "rotateVectorTest"
    val TAG_COROUTINE = "coroutineWorkerTest"
    val TAG_USAGE = "usageTest"
    val userKey = u_key

    //location variable
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationList:MutableList<Location>
    private lateinit var locationCallback: LocationCallback

    //rotate vector variable
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val mutableListOrientationAngles = mutableListOf<String>()

    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference
    var mTimestamp:Long = 0
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    lateinit var mChannel : NotificationChannel

    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    companion object var flag = false

    private fun printCallStack(){
        val sb = StringBuilder()
        sb.append("==================================\n  CALL STACK\n==================================\n");

        val e = Exception();
        val steArr = e.stackTrace;
        for (ste in steArr) {
            sb.append("  ");
            sb.append(ste.className);
            sb.append(".");
            sb.append(ste.methodName);
            sb.append(" #");
            sb.append(ste.lineNumber);
            sb.append("\n");
        }

        Log.d("test", sb.toString());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = coroutineScope {
        mTimestamp = System.currentTimeMillis()//공통으로 쓰일 timestamp
        val iterationRange = 60

        //debug
        printCallStack()

        var stats = ArrayList<UsageStat>()
        stats = showAppUsageStats(getAppUsageStats(mTimestamp-900000))

        initLocationParms()
        startLocationUpdates()

        val jobs =
            async {
                for (i in 1 .. iterationRange){
                    //Repeat every 1s
                    delay(1000L)
                    startMeasureRotateVector()
                    Log.d(TAG_COROUTINE, LocalDateTime.now().toString())
                }
                //stop location request when iteration was ended
                stopLocationUpdates()

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

            }

        if (isStopped) {
            fbDatabase = FirebaseDatabase.getInstance()
            dbReference = fbDatabase.reference
            dbReference.child("user").child(userKey).child("isRunning").setValue("false")
        }

        val progress = "데이터 전송 중"
        setForeground(createForegroundInfo(progress))

        Result.success()
    }

    private fun createForegroundInfo(progress:String):ForegroundInfo{
        val CHANNEL_ID = "$applicationContext.packageName-${R.string.app_name}2"
        val title = applicationContext.getString(R.string.channel_name)
        // This PendingIntent can be used to cancel the worker

        Log.d("setForeground","started")
        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(CHANNEL_ID,title, NotificationManager.IMPORTANCE_LOW)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(CHANNEL_ID:String, name:String, importance:Int){
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = "데이터 전송 중"
        Log.d("setForeground","created")

    }

    fun initLocationParms() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationList = mutableListOf()
        locationRequest = LocationRequest.create().apply {
            interval = 20 * 1000
            fastestInterval = 5 * 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    locationList.add(location)
                    Log.d(TAG_LOCATION,"(${location.latitude}, ${location.longitude})")
                }
            }
        }
    }

    fun getAppUsageStats(time:Long): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -1)//1분간의 stats 파악
        Log.d("calcal",cal.toString())

        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

    private fun startMeasureRotateVector(){

        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        //print roll, pitch, yaw
        //note that all three orientation angles are expressed in !!RADIANS!.
        Log.d(TAG_ROTATE, orientationAngles.contentToString())
        mutableListOrientationAngles.add(orientationAngles.contentToString())

    }

    private fun getLocation() {
        var ret = mutableMapOf<String, Double>()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG_LOCATION, "permission get failed")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
    }

}