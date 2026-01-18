package com.example.androidapp

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.model.RunTimeSensor
import com.example.androidapp.model.SensorType
import com.example.androidapp.utils.MQTTService
import com.example.androidapp.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

class SensorRunTimeViewModel : ViewModel() {
    private val sensors = mutableListOf<RunTimeSensor>()

    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context
    }

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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val randomLocation = Util.getRandomSloveniaLatLng()
                val latitude = randomLocation.first
                val longitude = randomLocation.second

                sendMqttUpdate(
                    deviceId = sensor.id,
                    updateType = "location",
                    data = mapOf(
                        "vehicleId" to sensor.id,
                        "latitude" to latitude,
                        "longitude" to longitude
                    )
                )
                Log.d("UPDATE_VEHICLE", "Location updated for vehicle ${sensor.id}: $latitude, $longitude")
            } catch (e: Exception) {
                Log.e("UPDATE_VEHICLE", "Failed to update location for vehicle ${sensor.id}", e)
            }
        }
    }


    private fun updateVehicleSensorAcceleration(sensor: RunTimeSensor) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val randomAcceleration = Random.nextDouble(0.5, 5.0)

                sendMqttUpdate(
                    deviceId = sensor.id,
                    updateType = "acceleration",
                    data = mapOf(
                        "vehicleId" to sensor.id,
                        "acceleration" to randomAcceleration
                    )
                )
                Log.d("UPDATE_VEHICLE", "Acceleration updated for vehicle ${sensor.id}: $randomAcceleration")
            } catch (e: Exception) {
                Log.e("UPDATE_VEHICLE", "Failed to update acceleration for vehicle ${sensor.id}", e)
            }
        }
    }

    private fun updateAccidentLocation(sensor: RunTimeSensor) {
        Log.d("UPDATE_ACCIDENT", "Update accident id:${sensor.id} location")
    }



    private fun sendMqttUpdate(deviceId: String, updateType: String, data: Map<String, Any>) {
        val ctx = context ?: return

        try {
            val json = JSONObject().apply {
                put("type", "update")
                put("updateType", updateType)
                data.forEach { (key, value) ->
                    when (value) {
                        is Number -> put(key, value)
                        is String -> put(key, value)
                        is Boolean -> put(key, value)
                        else -> put(key, value.toString())
                    }
                }
            }

            val intent = Intent(ctx, MQTTService::class.java).apply {
                action = "PUBLISH"
                putExtra("topic", "device/$deviceId/command/update")
                putExtra("payload", json.toString())
            }

            ctx.startService(intent)
        } catch (e: Exception) {
            Log.e("MQTT", "Failed to send MQTT update", e)
        }
    }
}