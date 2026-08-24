package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

enum class SpeedUnit(override val symbol: String, val metersPerSecond: BigDecimal) : ConverterUnit {
    MetersPerSecond("m/s", BigDecimal.ONE),
    KilometersPerHour(
        "km/h",
        BigDecimal("1000").divide(BigDecimal("3600"), MathContext.DECIMAL128)
    ),
    FeetPerSecond("ft/s", BigDecimal("0.3048")),
    MilesPerHour("mph", BigDecimal("0.44704"));

    override val category = ConverterCategory.Speed
}

fun convertSpeed(value: BigDecimal, source: SpeedUnit, target: SpeedUnit): BigDecimal =
    value.multiply(source.metersPerSecond).divide(target.metersPerSecond, MathContext.DECIMAL128)
