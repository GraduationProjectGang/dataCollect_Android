package com.example.datacollect_android.activity

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.datacollect_android.fragment.PopUpFragment
import com.example.datacollect_android.R
import kotlinx.android.synthetic.main.activity_tutorial3.*

class Tutorial3Activity : AppCompatActivity() {
    val fm: FragmentManager = supportFragmentManager
    val popup = PopUpFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial3)
        var granted = false
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED)
            Log.d("frafra",granted.toString())
        }

        nextButton3.setOnClickListener {
            popup.show(fm,"tag")
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
        Log.w("UMA_worker", prefs.getBoolean(getString(R.string.pref_previously_started), false).toString())

    }
    fun updateFragment(){
        val fragTransaction = fm.beginTransaction()
        fragTransaction.detach(popup)
        fragTransaction.attach(popup)
        Log.d("frafra","fragment updated")
    }
}
