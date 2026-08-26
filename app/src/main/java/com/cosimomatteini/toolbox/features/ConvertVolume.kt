package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.VolumeUnit
import com.cosimomatteini.toolbox.domain.convertVolume
import java.math.BigDecimal

class ConvertVolume {
    val units = VolumeUnit.entries

    operator fun invoke(value: BigDecimal, source: VolumeUnit, target: VolumeUnit): BigDecimal =
        convertVolume(value, source, target)
}
