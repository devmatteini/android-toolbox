package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.AreaUnit
import com.cosimomatteini.toolbox.domain.convertArea
import java.math.BigDecimal

class ConvertArea {
    val units = AreaUnit.entries

    operator fun invoke(value: BigDecimal, source: AreaUnit, target: AreaUnit): BigDecimal =
        convertArea(value, source, target)
}
