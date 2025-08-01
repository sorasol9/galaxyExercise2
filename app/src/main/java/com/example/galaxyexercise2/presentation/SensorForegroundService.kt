package com.example.galaxyexercise2.presentation

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.galaxyexercise2.presentation.MqttPublisher
import org.json.JSONObject

class SensorForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "SensorServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var mqttPublisher: MqttPublisher

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("센서 데이터 전송 중")
            .setContentText("MQTT를 통해 서버로 데이터를 전송합니다")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        mqttPublisher = MqttPublisher(this)
    }

    fun sendSensorData(heartRate: Double?, speed: Double?, steps: Double?) {
        val json = JSONObject().apply {
            put("heartRate", heartRate)
            put("speed", speed)
            put("steps", steps)
            put("timestamp", System.currentTimeMillis())
        }
        mqttPublisher.publish("sensor/data", json.toString())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sensor Data Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}