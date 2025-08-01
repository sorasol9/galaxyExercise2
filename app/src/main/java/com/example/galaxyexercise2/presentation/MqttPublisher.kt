package com.example.galaxyexercise2.presentation

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID

class MqttPublisher(private val context: Context) {

    private val brokerUrl = "tcp://broker.hivemq.com:1883" // 또는 Mosquitto 서버 주소
    private val clientId = UUID.randomUUID().toString()
    private val mqttClient: MqttAndroidClient = MqttAndroidClient(context, brokerUrl, clientId)

    init {
        connect()
    }

    private fun connect() {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
        }

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "Connected to broker")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Failed to connect: ${exception?.message}")
            }
        })
    }

    fun publish(topic: String, message: String) {
        if (!mqttClient.isConnected) {
            Log.e("MQTT", "Client not connected. Cannot publish message.")
            return
        }

        val mqttMessage = MqttMessage(message.toByteArray()).apply {
            qos = 1
            isRetained = false
        }

        mqttClient.publish(topic, mqttMessage, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "Message published: $message")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Publish failed: ${exception?.message}")
            }
        })
    }
}
