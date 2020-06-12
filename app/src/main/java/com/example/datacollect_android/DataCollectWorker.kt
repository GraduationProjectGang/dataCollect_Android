package com.example.datacollect_android

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.time.LocalDateTime

class DataCollectWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    override val coroutineContext = Dispatchers.IO

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = coroutineScope {
        var i = 0;
        val jobs =

            async {
                for (i in 1 .. 10){
                    delay(1000L)
                    Log.d("coroutine", LocalDateTime.now().toString())
                }
            }

        // awaitAll will throw an exception if a download fails, which CoroutineWorker will treat as a failure
        Result.success()
    }

}