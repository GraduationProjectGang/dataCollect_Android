package com.example.datacollect_android

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_user_main.*
import java.util.*
import java.util.concurrent.TimeUnit

lateinit var u_key: String


class UserMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        init()
    }

    private fun createWorker() {//init Periodic work

        val uniqueWorkName = "DataCollectWorker"

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        //15분 마다 반복
        val collectRequest =
            PeriodicWorkRequestBuilder<DataCollectWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("TAG")
                .build()

        //WorkManager에 enqueue
        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                collectRequest
            )
    }

    fun init() {
        val mystring = "프로젝트 가이드 다시보기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        tutorialAgain.setText(content)
        tutorialAgain.setOnClickListener {
            val intent = Intent(this, Tutorial1Activity::class.java)
            startActivity(intent)
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
        usercode.text =
            "Usercode: " + prefs.getString(getString(R.string.pref_previously_logined), "null")
        u_key = prefs.getString(getString(R.string.pref_previously_logined), "null")!!


        Log.w(
            "UMA_worker",
            prefs.getBoolean(getString(R.string.pref_previously_started), false).toString()
        )

        if (!prefs.getBoolean(getString(R.string.pref_previously_started), false)) {
            var edit = prefs.edit() as SharedPreferences.Editor
            edit.putBoolean(getString(R.string.pref_previously_started), true)
            edit.commit()

            setAlarmAt(10)
            createWorker()
        }

        button_survey.setOnClickListener {
            val intent = Intent(this, StressCollectActivity::class.java)
            startActivity(intent)
        }


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
        Log.d("alarmset","main alarm set${time}")
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra("time",time)
        val pendingIntent =
            PendingIntent.getBroadcast(this, time, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
    }


    class App : Application() {
        init {
            instance = this
        }

        companion object {
            private var instance: App? = null

            fun context(): Context {
                return instance!!.applicationContext
            }
        }
    }
}