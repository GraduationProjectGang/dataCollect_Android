package com.example.datacollect_android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val INTERNET_REQUEST = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        notifySurvey()//설문 알림
        //TODO:적절한 시간에 설문 알림
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
                return false
            }
        }
        return true
    }

    fun initPermission() {
        if(checkAppPermission(arrayOf(android.Manifest.permission.INTERNET))) {
            Toast.makeText(this, "인터넷 권한 승인됨", Toast.LENGTH_SHORT).show()
        }
        else {
            askPermission(arrayOf(android.Manifest.permission.INTERNET), INTERNET_REQUEST)
        }
    }


}
