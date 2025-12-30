package com.example.androidapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.model.RunTimeSensor
import com.example.androidapp.model.SensorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorRunTimeViewModel : ViewModel() {
    private val sensors = mutableListOf<RunTimeSensor>()

    init {
        startTickLoop()
    }

    fun addSensor(sensor: RunTimeSensor) {
        Log.d("SENSOR_ADDED", sensor.toString())
        sensors.add(sensor)
    }

    private fun startTickLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val now = System.currentTimeMillis()

                sensors.forEach { sensor ->
                    val elapsedTime = (now - sensor.lastExecution) / 60_000L

                    if (elapsedTime >= sensor.freqMin) {
                        sendUpdate(sensor)
                        sensor.lastExecution = now
                    }
                }
                delay(5_000L)
            }
        }
    }

    private fun sendUpdate(sensor: RunTimeSensor) {
        when (sensor.type) {
            SensorType.VEHICLE_LOCATION -> updateVehicleSensorLocation(sensor)
            SensorType.VEHICLE_ACCELERATION -> updateVehicleSensorAcceleration(sensor)
            SensorType.ACCIDENT_LOCATION -> updateAccidentLocation(sensor)
        }
    }

    private fun updateVehicleSensorLocation(sensor: RunTimeSensor) {
        Log.d("UPDATE_VEHICLE", "Update vehicle id:${sensor.id} location")
    }

    private fun updateVehicleSensorAcceleration(sensor: RunTimeSensor) {
        Log.d("UPDATE_VEHICLE", "Update vehicle id:${sensor.id} acceleration")
    }

    private fun updateAccidentLocation(sensor: RunTimeSensor) {
        Log.d("UPDATE_ACCIDENT", "Update accident id:${sensor.id} location")
    }
}