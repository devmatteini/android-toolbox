package com.cosimomatteini.toolbox.infrastructure

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import com.cosimomatteini.toolbox.domain.CompassHeading
import com.cosimomatteini.toolbox.domain.CompassSensor
import com.cosimomatteini.toolbox.domain.CompassSensorReading
import com.cosimomatteini.toolbox.domain.MagneticAccuracy
import com.cosimomatteini.toolbox.domain.MagneticFieldReading
import com.cosimomatteini.toolbox.domain.magneticFieldStrength
import com.cosimomatteini.toolbox.domain.normalizeHeading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOf

class AndroidCompassSensor(private val context: Context) : CompassSensor {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    override fun readings(): Flow<CompassSensorReading> {
        val rotationVector = rotationVectorSensor ?: return flowOf(CompassSensorReading.Unavailable)

        return callbackFlow {
            var latestOrientation: CompassOrientation? = null
            var magneticField: MagneticFieldReading = MagneticFieldReading.Unavailable
            var magneticAccuracy = MagneticAccuracy.Unreliable

            fun sendReading() {
                latestOrientation?.let {
                    trySend(
                        CompassSensorReading.Heading(
                            value = it.heading,
                            pitchDegrees = it.pitchDegrees,
                            rollDegrees = it.rollDegrees,
                            magneticField = magneticField
                        )
                    )
                }
            }

            val orientationListener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val displayRotationMatrix = FloatArray(9)
                private val orientation = FloatArray(3)

                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    remapForDisplayRotation(rotationMatrix, displayRotationMatrix)
                    SensorManager.getOrientation(displayRotationMatrix, orientation)
                    latestOrientation = CompassOrientation(
                        heading = normalizeHeading(
                            Math.toDegrees(orientation[0].toDouble()).toFloat()
                        ),
                        pitchDegrees = Math.toDegrees(orientation[1].toDouble()).toFloat(),
                        rollDegrees = Math.toDegrees(orientation[2].toDouble()).toFloat()
                    )
                    sendReading()
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            val magneticFieldListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    magneticField = MagneticFieldReading.Available(
                        strengthMicroteslas = magneticFieldStrength(
                            x = event.values[0],
                            y = event.values[1],
                            z = event.values[2]
                        ),
                        accuracy = magneticAccuracy
                    )
                    sendReading()
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    magneticAccuracy = accuracy.toMagneticAccuracy()
                    magneticField = (magneticField as? MagneticFieldReading.Available)?.copy(
                        accuracy = magneticAccuracy
                    ) ?: MagneticFieldReading.Unavailable
                    sendReading()
                }
            }

            if (!sensorManager.registerListener(
                    orientationListener,
                    rotationVector,
                    SensorManager.SENSOR_DELAY_GAME
                )
            ) {
                trySend(CompassSensorReading.Unavailable)
                close()
                return@callbackFlow
            }

            if (magneticFieldSensor != null) {
                sensorManager.registerListener(
                    magneticFieldListener,
                    magneticFieldSensor,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

            awaitClose {
                sensorManager.unregisterListener(orientationListener)
                sensorManager.unregisterListener(magneticFieldListener)
            }
        }.conflate()
    }

    private fun remapForDisplayRotation(rotationMatrix: FloatArray, output: FloatArray) {
        val displayRotation = context.display?.rotation ?: Surface.ROTATION_0
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            displayRotation.xAxis(),
            displayRotation.yAxis(),
            output
        )
    }
}

private data class CompassOrientation(
    val heading: CompassHeading,
    val pitchDegrees: Float,
    val rollDegrees: Float
)

private fun Int.toMagneticAccuracy(): MagneticAccuracy = when (this) {
    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> MagneticAccuracy.High
    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> MagneticAccuracy.Medium
    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> MagneticAccuracy.Low
    else -> MagneticAccuracy.Unreliable
}

private fun Int.xAxis(): Int = when (this) {
    Surface.ROTATION_90 -> SensorManager.AXIS_Y
    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X
    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y
    else -> SensorManager.AXIS_X
}

private fun Int.yAxis(): Int = when (this) {
    Surface.ROTATION_90 -> SensorManager.AXIS_MINUS_X
    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_Y
    Surface.ROTATION_270 -> SensorManager.AXIS_X
    else -> SensorManager.AXIS_Y
}
