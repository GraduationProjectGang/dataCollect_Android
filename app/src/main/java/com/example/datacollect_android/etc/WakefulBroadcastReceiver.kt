package com.example.datacollect_android.etc

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver

class WakefulBroadcastReceiver(): WakefulBroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val obj = intent!!.getParcelableExtra<Parcelable>("messenger")
        val service = Intent(context, WakefulIntentService::class.java)
//        service.putExtra("messenger", obj)
//        val eta = intent!!.getLongExtra(
//            "ETA",
//            -1
//        )
        Log.d("wakeful", "broadcast receiver")

        startWakefulService(context, service)
    }
}