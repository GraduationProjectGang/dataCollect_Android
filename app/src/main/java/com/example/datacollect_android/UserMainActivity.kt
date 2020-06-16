package com.example.datacollect_android

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_user_main.*


class UserMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        init()
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
        usercode.text = "Usercode: " + prefs.getString(getString(R.string.pref_previously_logined), "0000")

        //첫 실행이면 SignInActivity 실행
        var previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false)
        if(!previouslyStarted)
        {
            var edit = prefs.edit() as SharedPreferences.Editor
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}