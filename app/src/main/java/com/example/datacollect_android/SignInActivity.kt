package com.example.datacollect_android

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.io.File
import java.io.FileWriter

class SignInActivity : AppCompatActivity() {

    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference

    lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_sign_in)
        super.onCreate(savedInstanceState)
        //initPermission()
        initFirebase()

        init()
    }

    fun init() {

        val gender_array = arrayOf("남성","여성")
        val grade_array = arrayOf("1","2","3","4")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gender_array)
        user_gender.adapter = genderAdapter
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, grade_array)
        user_grade.adapter = gradeAdapter




        button_start.setOnClickListener {
            val nickname =  user_nickname.text.toString()
            val phonenum = user_phonenum.text.toString()
            val gender = user_gender.selectedItem.toString()
            val grade = user_grade.selectedItem.toString().toInt()

            val Listener = object: ChildEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    Log.e("LI_FBError", p0.toException().toString())
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                    val key = p0.getValue(UserInfo::class.java)
                    Log.w("LI_KEY", key!!.uniqueKey)
                    val file = File(Environment.getExternalStorageDirectory().absolutePath+"/datacollect.txt")
                    var fw = FileWriter(file)
                    fw.write(key!!.uniqueKey)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

            }

            dbReference.addChildEventListener(Listener)

            userInfo = UserInfo(nickname, phonenum, "123", gender, grade)
            dbReference.child("user").push().setValue(userInfo)


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



}