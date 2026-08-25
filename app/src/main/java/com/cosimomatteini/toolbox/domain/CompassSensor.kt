package com.cosimomatteini.toolbox.domain

import kotlinx.coroutines.flow.Flow

interface CompassSensor {
    fun readings(): Flow<CompassSensorReading>
}

sealed interface CompassSensorReading {
    data class Heading(
        val value: CompassHeading,
        val pitchDegrees: Float,
        val rollDegrees: Float,
        val magneticField: MagneticFieldReading
    ) : CompassSensorReading

    data object Unavailable : CompassSensorReading
}

sealed interface MagneticFieldReading {
    data class Available(val strengthMicroteslas: Float, val accuracy: MagneticAccuracy) :
        MagneticFieldReading

    data object Unavailable : MagneticFieldReading
}

enum class MagneticAccuracy {
    Unreliable,
    Low,
    Medium,
    High
}
