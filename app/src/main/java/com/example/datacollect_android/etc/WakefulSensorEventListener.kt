package com.example.datacollect_android.etc

import android.app.NotificationChannel
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.datacollect_android.activity.u_key
import com.example.datacollect_android.data_class.UsageStat
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class WakefulSensorEventListener(context: Context): SensorEventListener {
    val TAG_LOCATION = "LocationTest"
    val TAG_ROTATE = "rotateVectorTest"
    val TAG_COROUTINE = "coroutineWorkerTest"
    val TAG_USAGE = "usageTest"
    val userKey = u_key

    lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationList:MutableList<Location>
    lateinit var locationCallback: LocationCallback

    //rotate vector variable
    lateinit var sensorManager: SensorManager
    val accelerometerReading = FloatArray(3)
    val magnetometerReading = FloatArray(3)
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)
    val mutableListOrientationAngles = mutableListOf<String>()

    val context = context

    fun initLocationParms(): MutableList<Location> {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
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
        return locationList
    }




    fun measureRotateVector(): String {

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
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

        return orientationAngles.contentToString()
    }

    private fun getLocation() {
        var ret = mutableMapOf<String, Double>()
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG_LOCATION, "permission get failed")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
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