package com.example.datacollect_android

data class Stress_st(val timestamp: String, val stressCount: String, val index: String, val date: String) {
    constructor() : this("", "", "", "") {
    }
}