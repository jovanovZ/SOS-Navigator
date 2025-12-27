package com.example.androidapp.model

data class Vehicle (
    val id: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type : String = "",
    val acceleration : Double = 0.0
)