package com.example.androidapp.model

data class Vehicle (
    val id: String = "",
    val location: String = "",
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val endLongitude: Double = 0.0,
    val type : String = "",
    val acceleration : Double = 0.0
)