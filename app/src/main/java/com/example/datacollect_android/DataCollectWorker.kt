package com.example.datacollect_android

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemServiceName
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.time.LocalDateTime

class DataCollectWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), SensorEventListener {
    //reference doc link
    //https://developer.android.com/topic/libraries/architecture/workmanager/advanced/coroutineworker

    //rotate sensor variable

    val TAG_LOCATION = "LOCATIONTEST"
    val TAG_ROTATE = "ROTATEVECTORTEST"

    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private lateinit var mLocation: Location

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = coroutineScope {
        val iterationRange = 60
        val jobs =
            async {
                for (i in 1 .. iterationRange){
                    //Repeat every 1s
                    delay(1000L)
                    getRotateVector()
                    getLocation()
                    Log.d("coroutineTest", LocalDateTime.now().toString())

                }
            }
        jobs.join()
        Result.success()
    }


    private fun getRotateVector(){

        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mMagneticfield = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var mMagentic = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
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

    }
    private fun getLocation() {
        var ret = mutableMapOf<String, Double>()
        if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG_LOCATION, "permission get failed")
            return
        }

        var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if(location == null) {
                    Log.e(TAG_LOCATION, "location get fail")

                } else {
                    //print latitude, latitude
                    Log.d(TAG_LOCATION, "${location.latitude} , ${location.longitude}")
                }
                mLocation = location
            }
            .addOnFailureListener {
                Log.e(TAG_LOCATION, "location error is ${it.message}")
                it.printStackTrace()
            }

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