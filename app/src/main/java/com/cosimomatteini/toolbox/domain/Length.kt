package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

enum class LengthUnit(override val symbol: String, val meters: BigDecimal) : ConverterUnit {
    Millimeter("mm", BigDecimal("0.001")),
    Centimeter("cm", BigDecimal("0.01")),
    Meter("m", BigDecimal.ONE),
    Kilometer("km", BigDecimal("1000")),
    Inch("in", BigDecimal("0.0254")),
    Foot("ft", BigDecimal("0.3048")),
    Yard("yd", BigDecimal("0.9144")),
    Mile("mi", BigDecimal("1609.344"));

    override val category = ConverterCategory.Length
}

fun convertLength(value: BigDecimal, source: LengthUnit, target: LengthUnit): BigDecimal =
    value.multiply(source.meters).divide(target.meters, MathContext.DECIMAL128)
