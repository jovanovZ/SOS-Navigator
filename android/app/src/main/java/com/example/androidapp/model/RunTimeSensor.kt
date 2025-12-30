package com.example.androidapp.model

data class RunTimeSensor (
    val id: String,
    val type: SensorType,
    val freqMin: Int,
    var lastExecution: Long = System.currentTimeMillis()
    )