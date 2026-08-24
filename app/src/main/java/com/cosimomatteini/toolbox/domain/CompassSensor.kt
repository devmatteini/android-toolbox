package com.cosimomatteini.toolbox.domain

import kotlinx.coroutines.flow.Flow

interface CompassSensor {
    fun readings(): Flow<CompassSensorReading>
}

sealed interface CompassSensorReading {
    data class Heading(val value: CompassHeading) : CompassSensorReading

    data object Unavailable : CompassSensorReading
}
