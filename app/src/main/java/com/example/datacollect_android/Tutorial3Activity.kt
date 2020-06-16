package com.example.datacollect_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_tutorial2.*
import kotlinx.android.synthetic.main.activity_tutorial3.*

class Tutorial3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial3)
        nextButton3.setOnClickListener {

            val fm: FragmentManager = supportFragmentManager
            val popup = PopUpFragment()
            popup.show(fm,"tag")
        }

        tutorial3.setOnTouchListener(object:Tutorial1Activity.OnSwipeTouchListener(applicationContext){
            override fun onSwipeLeft() {
                val fm: FragmentManager = supportFragmentManager
                val popup = PopUpFragment()
                popup.show(fm,"tag")
            }
            override fun onSwipeRight() {
                onBackPressed()
                val intent = Intent(applicationContext,Tutorial2Activity::class.java) //다음이어질 액티비티
                startActivity(intent)
                overridePendingTransition(0, 0)

                finish()
            }

        })


    }
}
