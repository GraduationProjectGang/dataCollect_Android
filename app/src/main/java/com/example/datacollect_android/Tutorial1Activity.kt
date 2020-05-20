package com.example.datacollect_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_tutorial1.*

class Tutorial1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial1)
        nextButton1.setOnClickListener {
            val intent = Intent(this,Tutorial2Activity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
    }
}
