package com.cosimomatteini.toolbox.infrastructure

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.cosimomatteini.toolbox.domain.CompassSensor
import com.cosimomatteini.toolbox.domain.CompassSensorReading
import com.cosimomatteini.toolbox.domain.normalizeHeading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

class AndroidCompassSensor(context: Context) : CompassSensor {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    override fun readings(): Flow<CompassSensorReading> {
        val sensor = rotationVectorSensor ?: return flowOf(CompassSensorReading.Unavailable)

        return callbackFlow {
            val listener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val orientation = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val heading = Math.toDegrees(orientation[0].toDouble()).toFloat()

                    trySend(CompassSensorReading.Heading(normalizeHeading(heading)))
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            if (!sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)) {
                trySend(CompassSensorReading.Unavailable)
                close()
                return@callbackFlow
            }

            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }
}
