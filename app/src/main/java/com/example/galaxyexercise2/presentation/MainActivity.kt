/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.galaxyexercise2.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.*
import androidx.health.services.client.endExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.lifecycleScope
import com.example.galaxyexercise2.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var updateCallback: ExerciseUpdateCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            // ExerciseClient 초기화
            exerciseClient = HealthServices.getClient(this@MainActivity).exerciseClient
            // 콜백 등록
            updateCallback = object : ExerciseUpdateCallback {
                override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    val heartRateData =
                        update.latestMetrics.getData(DataType.HEART_RATE_BPM)

                    if(heartRateData.isNotEmpty()){
                        val bpm = heartRateData.last().value
                        Log.d("SENSOR","Heart Rate: $bpm")
                    }

                    val speedData =
                        update.latestMetrics.getData(DataType.SPEED)

                    if(speedData.isNotEmpty()){
                        val speed = speedData.last().value
                        Log.d("SENSOR", "Speed: $speed")
                    }

                    val stepsData =
                        update.latestMetrics.getData(DataType.STEPS)

                    if(stepsData.isNotEmpty()){
                        val step = stepsData.last().value
                        Log.d("SENSOR", "Steps: $step")
                    }
                }

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {  }

                override fun onRegistered() {}

                override fun onRegistrationFailed(throwable: Throwable) {}
            }

            exerciseClient.setUpdateCallback(updateCallback)
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startExercise()
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopExercise()
        }
    }

    private fun startExercise() {
        val config = ExerciseConfig.Builder(ExerciseType.WALKING)
            .setDataTypes(
                setOf(
                    DataType.HEART_RATE_BPM,
                    DataType.SPEED,
                    DataType.STEPS
                )
            )
            .build()

        lifecycleScope.launch {
            try {
                exerciseClient.startExercise(config)
                Log.d("EXERCISE", "운동 시작됨")
            } catch (e: Exception) {
                Log.e("EXERCISE", "운동 시작 실패: ${e.message}")
            }
        }
    }

    private fun stopExercise() {
        lifecycleScope.launch {
            try {
                exerciseClient.endExercise()
                Log.d("EXERCISE", "운동 종료됨")
            } catch (e: Exception) {
                Log.e("EXERCISE", "운동 종료 실패: ${e.message}")
            }
        }
    }
}