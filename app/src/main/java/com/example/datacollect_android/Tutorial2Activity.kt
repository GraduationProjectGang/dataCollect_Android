package com.example.datacollect_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_tutorial2.*

class Tutorial2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial2)
        nextButton2.setOnClickListener {
            val intent = Intent(this,Tutorial3Activity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
        tutorial2.setOnTouchListener(object:Tutorial1Activity.OnSwipeTouchListener(applicationContext){
            override fun onSwipeLeft() {
                val intent = Intent(applicationContext,Tutorial3Activity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            override fun onSwipeRight() {
                onBackPressed()
                val intent = Intent(applicationContext,Tutorial1Activity::class.java) //다음이어질 액티비티
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }

        })
    }
}
