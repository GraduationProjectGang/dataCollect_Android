package com.example.datacollect_android

data class RotateVectorStress(var angleList: MutableList<String>, val timestamp: String,val index:Int, val time: Long) {
    init {
        angleList = mutableListOf()
    }
}