package com.example.datacollect_android

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_stress_collect.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class StressCollectActivity : AppCompatActivity() {
    lateinit var time:LocalDateTime

    lateinit var fbDatabase: FirebaseDatabase
    lateinit var dbReference: DatabaseReference
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    lateinit var prefs : SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stress_collect)
        time = LocalDateTime.now()

        init()
    }

    fun init(){

        fbDatabase = FirebaseDatabase.getInstance()
        dbReference = fbDatabase.reference

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())

        val key = prefs.getString(getString(R.string.pref_previously_logined), "null")

        Log.w("SCA_init", key)

        var input = intArrayOf(9,9,9,9) //기본값 설정
        stressRadio1.setOnCheckedChangeListener { radioGroup, i ->
            //radiobutton 값 받아서 input에 저장
            input.set(0,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)
        }
        stressRadio2.setOnCheckedChangeListener { radioGroup, i ->
            input.set(1,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)

        }
        stressRadio3.setOnCheckedChangeListener { radioGroup, i ->
            input.set(2,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)
        }
        stressRadio4.setOnCheckedChangeListener { radioGroup, i ->
            input.set(3,findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48)
        }

        stressFinishBtn.setOnClickListener{

            if(input.contains(9)){
                Toast.makeText(this,"모든 질문에 답해주세요",Toast.LENGTH_SHORT).show()
            }else{
                when(input[1]){
                    0-> input[1]=4
                    1-> input[1]=3
                    3-> input[1]=1
                    4-> input[1]=0
                }
                when(input[2]){
                    0-> input[2]=4
                    1-> input[2]=3
                    3-> input[2]=1
                    4-> input[2]=0
                }

                //다 더한 점수 저장, 만점: 16점
                var score = 0
                for(i in input){
                    score = score + i
                }
                Log.d("surveyscore",score.toString())
                Toast.makeText(this,"점수: ${score}",Toast.LENGTH_SHORT).show()

                var stCount = prefs.getInt(getString(R.string.stress_collect_count), 0)

                val curTime = System.currentTimeMillis()

                if (stCount == 0) {
                    showAppUsageStats(getAppUsageStats(curTime))
                }
                else {
                    dbReference.child("user").child(key!!).child("stress").orderByChild("index")
                        .equalTo(stCount.toString()).addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                Log.w("SCA_Error", p0.toString())
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                for (children in p0.children) {
                                    Log.w("SCA_Usage", children.value.toString())
                                    val previousTime = children.getValue(Stress_st::class.java)
                                    Log.w("SCA_Stress", previousTime.toString())
                                    showAppUsageStats(getAppUsageStats(curTime - previousTime!!.timestamp.toLong()))
                                }
                            }

                        })
                }

                dbReference.child("user").orderByKey().equalTo(key).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.w("SCA_Error", p0.toString())
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (children in p0.children) {
                            Log.w("SCA_Right", children.key)
                            val st = Stress_st(curTime.toString(), score.toString(), stCount.toString())
                            dbReference.child("user").child(children.key!!).child("stress").push().setValue(st)
                        }
                    }
                })

                stCount++
                val edit = prefs.edit() as SharedPreferences.Editor
                edit.putInt(getString(R.string.stress_collect_count), stCount)
                edit.commit()
            }

            finish()

        }
    }

    fun getAppUsageStats(time:Long): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -1)//1분간의 stats 파악
        Log.d("calcal",cal.toString())

        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,time, System.currentTimeMillis()
        )

        Log.d("appusing", queryUsageStats.size.toString())
        return queryUsageStats
    }

    fun showAppUsageStats(usageStats: MutableList<UsageStats>) {

        val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
        Log.d("appusing", usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })
        var statsArr = ArrayList<UsageStat>()

        usageStats.forEach {
            if(it.lastTimeUsed>0){
                statsArr.add(UsageStat(it.packageName,dateFormat.format(it.lastTimeUsed),it.totalTimeInForeground))
                Log.d("appusing",statsArr.last().toString())
            }
        }
        Log.d("appusing","statsArrLen: ${statsArr.size}")

        var usage = UsageStatsCollection(ArrayList(), prefs.getInt(getString(R.string.stress_collect_count), 0)!!.toString(), System.currentTimeMillis().toString())
        usage.statsList = statsArr

        dbReference.child("user").child(prefs.getString(getString(R.string.pref_previously_logined), "null")!!).child("usagestats").push().setValue(usage)
    }
}
