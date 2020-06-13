package com.example.datacollect_android

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*












import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    val MULTIPLE_REQUEST = 1234

    var permissionArr = arrayOf(
        android.Manifest.permission.PACKAGE_USAGE_STATS,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference

    lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_sign_in)
        super.onCreate(savedInstanceState)
        initPermission()
        initFirebase()
    }
    fun init() {

        val gender_array = arrayOf("남성","여성")
        val grade_array = arrayOf("1","2","3","4")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gender_array)
        user_gender.adapter = genderAdapter
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gender_array)
        user_gender.adapter = genderAdapter


        button_start.setOnClickListener {
            val nickname =  user_nickname.text
            val phonenum = user_phonenum.text
            val gender = (user_gender as TextView).text
            val grade = (user_grade as TextView).text



        }


    }

    override fun onStart() {
        super.onStart()
        checkPreviousUser()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }


















    fun initFirebase() {
        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference



    }

    fun checkPreviousUser():Boolean {

        return true
    }

    fun initPermission() {
        var rejectedPermissionList = ArrayList<String>()

        for (permission in permissionArr) {
            if (checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(this, "권한 모두 승인됨", Toast.LENGTH_SHORT).show()
            }
        }
    }


}