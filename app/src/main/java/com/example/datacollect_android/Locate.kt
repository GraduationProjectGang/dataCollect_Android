package com.example.datacollect_android

import android.location.Location

data class Locate(var locationList: MutableList<Location>, val timestmamp: String) {
    init{
        locationList = mutableListOf()
    }
}