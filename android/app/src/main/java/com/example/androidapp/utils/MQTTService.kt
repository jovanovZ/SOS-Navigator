package com.example.androidapp.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.androidapp.BuildConfig
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.UUID

class MQTTService : Service() {
    private val MQTT_URL = BuildConfig.MQTT_URL
    private lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onCreate() {
        super.onCreate()

        val clientId = "androidServerPublisher${UUID.randomUUID()}"
        mqttAndroidClient = MqttAndroidClient(this, MQTT_URL, clientId)
        val options = MqttConnectOptions().apply { isCleanSession = true }
        mqttAndroidClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                mqttAndroidClient.subscribe("device/+/command/#", 0)
                Log.d("MQTT", "Connected successfully")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Connection failed: ${exception?.message}")
            }
        })

        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun messageArrived(
                topic: String?,
                message: MqttMessage?
            ) {
                Log.d("MQTT", "Received from topic $topic -> $message")
            }

            override fun connectionLost(cause: Throwable?) {}
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
    }
    fun publish(topic: String, payload: String) {
        if (::mqttAndroidClient.isInitialized && mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(topic, payload.toByteArray(), 0, false)
            Log.d("MQTT", "Published to $topic -> $payload")
        } else {
            Log.e("MQTT", "Client not connected, cannot publish")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        if (::mqttAndroidClient.isInitialized && mqttAndroidClient.isConnected) {
            try {
                mqttAndroidClient.disconnect()
            } catch (e: Exception) {
                Log.e("MQTT", "Disconnect failed: ${e.message}")
            }
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "PUBLISH") {
            val topic = intent.getStringExtra("topic")
            val payload = intent.getStringExtra("payload")

            if (topic != null && payload != null) {
                publish(topic, payload)
            } else {
                Log.e("MQTT", "Missing topic or payload")
            }
        }
        return START_STICKY
    }
}
