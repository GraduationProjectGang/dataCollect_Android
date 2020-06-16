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


        //첫 실행이면 SignInActivity 실행
        val prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
        var previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false)
        if(!previouslyStarted)
        {
            var edit = prefs.edit() as SharedPreferences.Editor
            edit.putBoolean(getString(R.string.pref_previously_started),true)
            edit.commit()
        }
    }
}