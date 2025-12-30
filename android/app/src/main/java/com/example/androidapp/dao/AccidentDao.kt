package com.example.androidapp.dao

//razred za parsanje iz backenda
data class AccidentResponse(
    val message: String,
    val accident: AccidentDao
)

data class AccidentDao(
    val id: String,
    val typeOfAccident: String,
    val location: LocationDao,
    val locationFreq: Int
)
