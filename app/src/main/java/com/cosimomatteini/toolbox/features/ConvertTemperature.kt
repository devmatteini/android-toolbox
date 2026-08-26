package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.domain.convertTemperature
import java.math.BigDecimal

class ConvertTemperature {
    val units = TemperatureUnit.entries

    operator fun invoke(
        value: BigDecimal,
        source: TemperatureUnit,
        target: TemperatureUnit
    ): BigDecimal = convertTemperature(value, source, target)
}
