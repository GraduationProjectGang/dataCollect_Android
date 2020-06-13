package com.example.datacollect_android

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.time.LocalDateTime

class DataCollectWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    //reference doc link
    //https://developer.android.com/topic/libraries/architecture/workmanager/advanced/coroutineworker

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = coroutineScope {
        val iterationRange = 60
        val jobs =
            async {
                for (i in 1 .. iterationRange){
                    //Repeat every 1s
                    delay(1000L)
                    //getRotateVector()
                    val location = getLocation()
                    Log.d("coroutineTest", LocalDateTime.now().toString())
                }
            }

        Result.success()
    }
    private fun getRotateVector() : MutableList<Double>{
        return mutableListOf()
    }
    private fun getLocation() : MutableMap<String, Double>{
        val TAG = "locationTest"
        var ret = mutableMapOf<String, Double>()
        if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "permission get failed")
            return mutableMapOf()
        }

        var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if(location == null) {
                    Log.e(TAG, "location get fail")
                } else {
                    Log.d(TAG, "${location.latitude} , ${location.longitude}")
                    ret["latitude"] = location.latitude
                    ret["longitude"] = location.longitude
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "location error is ${it.message}")
                it.printStackTrace()
            }

        return ret
    }

}