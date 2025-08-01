package com.example.galaxyexercise2.presentation

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*
import org.json.JSONObject

class MqttPublisher(private val context: Context) {

    private lateinit var client: MqttAndroidClient
    private val serverUri = "tcp://broker.hivemq.com:1883" // ← 필요 시 IP로 변경
    private val topic = "sensor/data/sora9"
    private val clientId = "watch-client-" + UUID.randomUUID().toString()

    fun connect() {
        client = MqttAndroidClient(context, serverUri, clientId)

        val options = MqttConnectOptions().apply {
            isCleanSession = true
            connectionTimeout = 10
            keepAliveInterval = 20
        }

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.w("MQTT", "연결 끊김", cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // 필요 시 구현
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "메시지 전송 완료")
            }
        })

        client.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "MQTT 연결 성공")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "MQTT 연결 실패", exception)
            }
        })
    }

    fun publishSensorData(heartRate: Double?, speed: Double?, steps: Long?) {
        if (!client.isConnected) {
            Log.w("MQTT", "MQTT 연결 안 됨, 발행 실패")
            return
        }

        val json = JSONObject().apply {
            put("workerId", 1L)
            put("heartRate", heartRate)
            put("speed", speed)
            put("steps", steps)
        }

        val message = MqttMessage(json.toString().toByteArray()).apply {
            qos = 0
            isRetained = false
        }

        try {
            client.publish(topic, message)
            Log.d("MQTT", "메시지 발행 성공: $json")
        } catch (e: MqttException) {
            Log.e("MQTT", "메시지 발행 실패", e)
        }
    }

    fun disconnect() {
        if (::client.isInitialized && client.isConnected) {
            client.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "MQTT 연결 해제됨")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "연결 해제 실패", exception)
                }
            })
        }
    }
}