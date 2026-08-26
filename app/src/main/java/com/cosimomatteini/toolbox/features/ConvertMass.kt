package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.MassUnit
import com.cosimomatteini.toolbox.domain.convertMass
import java.math.BigDecimal

class ConvertMass {
    val units = MassUnit.entries

    operator fun invoke(value: BigDecimal, source: MassUnit, target: MassUnit): BigDecimal =
        convertMass(value, source, target)
}
