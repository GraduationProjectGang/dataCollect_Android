package com.example.datacollect_android

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_tutorial1.*
import android.provider.Settings


class Tutorial1Activity : AppCompatActivity() {

    val MULTIPLE_REQUEST = 1234

    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference

    var permissionArr = arrayOf(
        android.Manifest.permission.PACKAGE_USAGE_STATS,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial1)

        initPermission()

        addWhiteList()

        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference

        var i = 0

        //data class UserInfo( val userName: String? ="", val userPhone: String? = "", val userCode: Int = 0, val userGender: Int = 0, val userGrade: Int = 0) {


        nextButton1.setOnClickListener {
            val intent = Intent(this,Tutorial2Activity::class.java) //다음이어질 액티비티
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

    }

    fun addWhiteList() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        var isWhite = false
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhite = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
        }

        if (!isWhite) {
            val setdialog = AlertDialog.Builder(this)
            setdialog.setTitle("추가 설정이 필요합니다.")
                .setMessage("어플을 문제없이 사용하기 위해서는 해당 어플을 \"배터리 사용량 최적화\" 목록에서 \"제외\"해야 합니다. 설정화면으로 이동하시겠습니까?")
                .setPositiveButton("네") { dialog, which -> startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                .setNegativeButton("아니오") { dialog, which -> Toast.makeText(this, "설정을 취소했습니다.", Toast.LENGTH_SHORT).show() }
                .create()
                .show()
        }
    }


    fun initPermission() {
        var rejectedPermissionList = ArrayList<String>()

        for (permission in permissionArr) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                rejectedPermissionList.add(permission)
            }
        }
        if(rejectedPermissionList.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), MULTIPLE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_REQUEST -> {

            }
        }
    }

}
