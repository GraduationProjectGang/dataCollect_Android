package com.example.datacollect_android.data_class

data class RotateVector(var angleList: MutableList<String>, val timestamp: String) {
    init {
        angleList = mutableListOf()
    }
}