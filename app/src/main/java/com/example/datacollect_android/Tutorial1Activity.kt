package com.example.datacollect_android

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_tutorial1.*
import java.io.File
import java.io.FileWriter

class Tutorial1Activity : AppCompatActivity() {

    val MULTIPLE_REQUEST = 1234

    var permissionArr = arrayOf(
        android.Manifest.permission.PACKAGE_USAGE_STATS,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference

    lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial1)

        initPermission()

        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference

        val Listener = object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Log.e("T1_FBError", p0.toException().toString())
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (snap in p0.children) {
                    Log.d("T1_Child", snap.getValue().toString())
                }
            }

        }

        fbDatabase.getReference().addValueEventListener(Listener)

        var i = 0

        //data class UserInfo( val userName: String? ="", val userPhone: String? = "", val userCode: Int = 0, val userGender: Int = 0, val userGrade: Int = 0) {


        nextButton1.setOnClickListener {
            val intent = Intent(this,Tutorial2Activity::class.java) //다음이어질 액티비티
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        tutorial1.setOnTouchListener(object: OnSwipeTouchListener(this@Tutorial1Activity) {
            override fun onSwipeLeft() {
                val intent = Intent(applicationContext,Tutorial2Activity::class.java) //다음이어질 액티비티
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            override fun onSwipeRight() {
                onBackPressed()

            }
        })


    }
    open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

        private val gestureDetector: GestureDetector

        companion object {

            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100
        }

        init {
            gestureDetector = GestureDetector(ctx, GestureListener())
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {


            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                var result = false
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                            result = true
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom()
                        } else {
                            onSwipeTop()
                        }
                        result = true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                return result
            }


        }

        open fun onSwipeRight() {}

        open fun onSwipeLeft() {}

        open fun onSwipeTop() {}

        open fun onSwipeBottom() {}
    }
    override fun onStart() {
        super.onStart()
//        val file = File("user.txt")
//        if (file != null) {
//            finish()
//        }
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
                Toast.makeText(this, "권한 모두 승인됨", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
