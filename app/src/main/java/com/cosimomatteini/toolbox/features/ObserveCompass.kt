package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.CompassSensor
import com.cosimomatteini.toolbox.domain.CompassSensorReading
import kotlinx.coroutines.flow.Flow

class ObserveCompass(private val compassSensor: CompassSensor) {
    operator fun invoke(): Flow<CompassSensorReading> = compassSensor.readings()
}
