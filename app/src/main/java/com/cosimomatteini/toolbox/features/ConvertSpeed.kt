package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.SpeedUnit
import com.cosimomatteini.toolbox.domain.convertSpeed
import java.math.BigDecimal

class ConvertSpeed {
    val units = SpeedUnit.entries

    fun convert(value: BigDecimal, source: SpeedUnit, target: SpeedUnit): BigDecimal =
        convertSpeed(value, source, target)
}
