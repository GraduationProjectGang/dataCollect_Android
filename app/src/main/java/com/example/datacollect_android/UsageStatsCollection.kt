package com.example.datacollect_android

data class UsageStatsCollection(var statsList: ArrayList<UsageStat>, val index: String, val timestamp: Long, val date:String) {
    init {
        statsList = ArrayList()
    }
}