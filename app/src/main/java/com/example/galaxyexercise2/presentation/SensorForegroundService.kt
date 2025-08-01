package com.example.galaxyexercise2.presentation

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.endExercise
import androidx.health.services.client.startExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SensorForegroundService : Service() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var updateCallback: ExerciseUpdateCallback
    private lateinit var mqttPublisher: MqttPublisher

    // 코루틴 스코프 생성
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        mqttPublisher = MqttPublisher(this)
        mqttPublisher.connect()

        exerciseClient = HealthServices.getClient(this).exerciseClient
        updateCallback = object : ExerciseUpdateCallback {
            override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}

            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                val heartRateData = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                val speedData = update.latestMetrics.getData(DataType.SPEED)
                val stepsData = update.latestMetrics.getData(DataType.STEPS)

                // 여기서 MQTT 전송도 가능:
                sendSensorData(
                    heartRateData.lastOrNull()?.value,
                    speedData.lastOrNull()?.value,
                    stepsData.lastOrNull()?.value
                )
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}

            override fun onRegistered() {}

            override fun onRegistrationFailed(throwable: Throwable) {
                Log.e("SENSOR", "Callback 등록 실패", throwable)
            }
        }

        exerciseClient.setUpdateCallback(updateCallback)

        val config = ExerciseConfig.Builder(ExerciseType.WALKING)
            .setDataTypes(setOf(DataType.HEART_RATE_BPM, DataType.SPEED, DataType.STEPS))
            .build()

        serviceScope.launch {
            try {
                exerciseClient.startExercise(config)
                Log.d("EXERCISE", "운동 시작됨")
            } catch (e: Exception) {
                Log.e("EXERCISE", "운동 시작 실패", e)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // 코루틴 정리
        mqttPublisher.disconnect()
        serviceScope.launch {
            exerciseClient.endExercise()
        }
    }
    private fun sendSensorData(heartRate: Double?, speed: Double?, steps: Long?) {
        mqttPublisher.publishSensorData(heartRate, speed, steps)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}