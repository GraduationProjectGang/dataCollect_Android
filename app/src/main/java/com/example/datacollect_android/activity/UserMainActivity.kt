package com.example.datacollect_android.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.datacollect_android.etc.AlarmReceiver
import com.example.datacollect_android.etc.BootReceiver
import com.example.datacollect_android.etc.DataCollectWorker
import com.example.datacollect_android.R
import com.example.datacollect_android.etc.WakefulIntentService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_user_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class UserMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        init()
    }

    public fun createWorker() {//init Periodic work

        val uniqueWorkName = "DataCollectWorker"

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        //20분 마다 반복
        val collectRequest =
            OneTimeWorkRequestBuilder<DataCollectWorker>()
                .setConstraints(constraints)
                .addTag("DCWorker")
                .build()


        //WorkManager에 enqueue
//        WorkManager.getInstance(applicationContext)
//            .enqueueUniquePeriodicWork(
//                uniqueWorkName,
//                ExistingPeriodicWorkPolicy.REPLACE,
//                collectRequest
//            )
        val workManager = WorkManager.getInstance(applicationContext)
        workManager?.let {
            it.enqueue(collectRequest)
        }

//        workManager?.let {
//            it.enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.KEEP, collectRequest)
//            val statusLiveData = it.getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
//            statusLiveData.observe(this, androidx.lifecycle.Observer {
//                Log.w("workstatus", "state: ${it[0].state}")
//                if (it[0].state == WorkInfo.State.BLOCKED || it[0].state == WorkInfo.State.CANCELLED || it[0].state == WorkInfo.State.FAILED) {
//                    val fbDatabase = FirebaseDatabase.getInstance()
//                    val dbReference = fbDatabase.reference
//                    dbReference.child("user").child(u_key).child("isRunning").setValue("false")
//                }
//            })
//        }
        Log.d("fcm", "request enqueued")

    }

    fun cancelWork() {
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelAllWorkByTag("DCWorker")
    }

    fun init() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("fcm", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "token: " + token
                Log.d("fcm", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })


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


//            createWorker()
        }

        setAlarmAt(10)

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
        )

        var i = 0
        emergency.setOnClickListener {
            if (i == 0) {
                Toast.makeText(this, "감사합니다! 크크", Toast.LENGTH_SHORT).show()
                i++
            }
            else if (i > 0) {
                createWorker()
                Toast.makeText(this, "Worker Enqueued", Toast.LENGTH_SHORT).show()
            }
        }
//        createWorker()
    }

    fun setAlarmAt(RequestCode: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, RequestCode)
        }

        Log.d("alarmset","main alarm set${RequestCode}")
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra("time",RequestCode)

        val alarmUp = PendingIntent.getBroadcast(this, RequestCode, alarmIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        if (alarmUp) {
            Log.d("myTag", "Alarm is already active")
        }else{
            Log.d("myTag", "Alarm doesn't exist")
            val pendingIntent =
                PendingIntent.getBroadcast(this, RequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        }
    }
}