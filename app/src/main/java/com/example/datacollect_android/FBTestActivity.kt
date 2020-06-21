package com.example.datacollect_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_fbtest.*


class FBTestActivity : AppCompatActivity() {

    lateinit var dbReference: DatabaseReference
    lateinit var fbDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fbtest)

        init()
    }


    fun init() {

        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference

        button.setOnClickListener {
            dbReference.child("test").push().setValue((System.currentTimeMillis()/1000).toString())
        }

    }
}
