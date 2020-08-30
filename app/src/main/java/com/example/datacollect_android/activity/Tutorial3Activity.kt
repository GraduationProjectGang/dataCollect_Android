package com.example.datacollect_android.activity

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.datacollect_android.fragment.PopUpFragment
import com.example.datacollect_android.R
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

        val prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
        Log.w("UMA_worker", prefs.getBoolean(getString(R.string.pref_previously_started), false).toString())

    }
}
