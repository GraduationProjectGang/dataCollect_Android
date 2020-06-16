package com.example.datacollect_android

import java.util.*

data class UserInfo( val userName: String? ="", val userPhone: String? = "", val userCode: String? = "", val userGender: String? = "", val userGrade: Int = 0, val uniqueKey: String? ="") {
}

data class UsageStat(val packageName:String, val lastTimeUsed: String, val totalTimeInForeground:Long)