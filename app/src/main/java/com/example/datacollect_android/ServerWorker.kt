package com.example.datacollect_android

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ServerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val flag = false

        //do something

        if (flag) {



            return Result.success()
        }
        else {
            return Result.failure()
        }
    }
}