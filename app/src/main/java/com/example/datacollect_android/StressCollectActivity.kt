package com.example.datacollect_android

import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stress_collect.*
import java.time.LocalDateTime

class StressCollectActivity : AppCompatActivity() {
    lateinit var time:LocalDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stress_collect)
        time = LocalDateTime.now()
        init()
    }

    fun init(){


        var input = intArrayOf(9,9,9) //기본값 설정
        stressRadio1.setOnCheckedChangeListener { radioGroup, i ->
            //radiobutton 값 받아서 input에 저장
            input.set(0,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)
        }
        stressRadio2.setOnCheckedChangeListener { radioGroup, i ->
            input.set(1,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)

        }
        stressRadio3.setOnCheckedChangeListener { radioGroup, i ->
            input.set(2,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)
        }

        stressFinishBtn.setOnClickListener{
            if(input.contains(9)){
                Toast.makeText(this,"모든 질문에 답해주세요",Toast.LENGTH_SHORT).show()
            }else{
                //TODO: Add data to db
                Toast.makeText(this,"데이터 전송"+time,Toast.LENGTH_SHORT).show()

            }
        }
    }
}
