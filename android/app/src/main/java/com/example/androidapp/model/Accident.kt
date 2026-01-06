package com.example.androidapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Accident (
    val id: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type : String = "",
) : Parcelable