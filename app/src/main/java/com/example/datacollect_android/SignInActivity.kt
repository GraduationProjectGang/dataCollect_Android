package com.example.datacollect_android

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference
    lateinit var userInfo: UserInfo
    lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_sign_in)
        super.onCreate(savedInstanceState)

        //keyboard쓸 때 올라감
        keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
            onShowKeyboard = { keyboardHeight ->
                sv_root.run {
                    smoothScrollTo(scrollX, scrollY + keyboardHeight)
                }
            })

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

        val listener = object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.w("LI", p0.toString())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                Log.w("LI", p0.getValue(UserInfo::class.java).toString())
                //Log.w("LI_p1", p1)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        }

        dbReference.child("user").addChildEventListener(listener)

        button_start.setOnClickListener {
            val nickname =  user_nickname.text.toString()
            val phonenum = user_phonenum.text.toString()
            val gender = user_gender.selectedItem.toString()
            val grade = user_grade.selectedItem.toString().toInt()

            if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(phonenum) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(user_grade.selectedItem.toString())) {
                Toast.makeText(this, "빠진 항목 없이 다 작성해주세요!", Toast.LENGTH_SHORT).show()
            }else if (!TextUtils.isDigitsOnly(phonenum)){
                Toast.makeText(this, "휴대폰 번호를 형식에 맞게 입력해주세요!", Toast.LENGTH_SHORT).show()
            }else if (!(phonenum.length == 11)){
                Toast.makeText(this, "휴대폰 번호를 형식에 맞게 입력해주세요!", Toast.LENGTH_SHORT).show()
            }else if(!checkbox.isChecked){
                Toast.makeText(this, "개인정보활용에 동의해 주세요", Toast.LENGTH_SHORT).show()

            }
            else {
                userInfo = UserInfo(nickname, phonenum, gender, grade)
                //dbReference.child("user").push().setValue(userInfo)
                val key = dbReference.child("user").push().key
                Log.w("SI_", key)
                dbReference.child("user").child(key!!).setValue(userInfo)

                val prefs = PreferenceManager.getDefaultSharedPreferences(this.baseContext)
                var edit = prefs.edit() as SharedPreferences.Editor
                edit.putString(getString(R.string.pref_previously_logined), key)
                edit.putBoolean(getString(R.string.pref_previously_started), true)
                edit.commit()

                val intent = Intent(applicationContext,Tutorial1Activity::class.java) //다음이어질 액티비티
                startActivity(intent)
                Toast.makeText(this,"감사합니다! 튜토리얼을 시작합니다.",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this.baseContext)
        if (prefs.getString(getString(R.string.pref_previously_logined), "null") != "null") {

            finish()
        }

    }

    fun initFirebase() {
        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference
    }

}