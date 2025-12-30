package com.example.androidapp.dao

//razred za parsanje iz backenda
data class VehicleResponse(
    val message: String,
    val vehicle: VehicleDao
)

data class VehicleDao(
    val id: String,
    val locationStart: LocationDao,
    val locationEnd: LocationDao,
    val type: String,
    val acceleration: Double,
    val locationFreq: Int,     // minute
    val accelerationFreq: Int, // minute
    val timeStamp: String
)
