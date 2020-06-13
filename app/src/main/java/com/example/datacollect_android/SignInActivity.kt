package com.example.datacollect_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_sign_in.*

//<uses-permission android:name="com.google.android.things.permission.MANAGE_GNSS_DRIVERS" />
//<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
//<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
//<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />


class SignInActivity : AppCompatActivity() {
    val MULTIPLE_REQUEST = 1234

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    var permissionArr = arrayOf(
        android.Manifest.permission.PACKAGE_USAGE_STATS,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.INTERNET
        )

    val RC_SIGN_IN = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_sign_in)
        super.onCreate(savedInstanceState)
        initPermission()

        init()
    }
    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        //User(nickname,gender,grade,phonenum)


        val gender_array = arrayOf("남성","여성")
        val grade_array = arrayOf("1","2","3","4")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gender_array)
        user_gender.adapter = genderAdapter
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gender_array)
        user_gender.adapter = genderAdapter


        button_start.setOnClickListener {
            val nickname =  user_nickname.text
            val phonenum = user_phonenum.text




        }


    }
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            toMainActivity(firebaseAuth.currentUser)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            Log.d("resres", requestCode.toString())

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d("resres", task.toString())

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
            }

        }

    }



    fun signIn() {
        val signinIntent = googleSignInClient.signInIntent
        startActivityForResult(signinIntent, RC_SIGN_IN)
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            //updateUI
        }
    }

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("resres", "firebaseAuthWithGoogle:"+acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.w("resres", "firebaseAuthWithGoogle 성공", task.exception)
                toMainActivity(firebaseAuth?.currentUser)
            }
            else {
                Log.w("LoginActivity", "firebaseAuthWithGoogle 실패", task.exception)
                Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toMainActivity(user: FirebaseUser?) {
        if(user !== null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
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

            }
        }
    }


}