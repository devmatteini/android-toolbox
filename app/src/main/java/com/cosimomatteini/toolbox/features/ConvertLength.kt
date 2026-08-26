package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.convertLength
import java.math.BigDecimal

class ConvertLength {
    val units = LengthUnit.entries

    operator fun invoke(value: BigDecimal, source: LengthUnit, target: LengthUnit): BigDecimal =
        convertLength(value, source, target)
}
