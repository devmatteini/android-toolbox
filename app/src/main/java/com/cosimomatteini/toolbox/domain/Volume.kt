package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

enum class VolumeUnit(override val symbol: String, val millilitres: BigDecimal) : ConverterUnit {
    Millilitre("mL", BigDecimal.ONE),
    Litre("L", BigDecimal("1000")),
    CubicMeter("m³", BigDecimal("1000000")),
    FluidOunce("fl oz", BigDecimal("28.4130625")),
    Pint("pt", BigDecimal("568.26125")),
    Quart("qt", BigDecimal("1136.5225")),
    ImperialGallon("gal", BigDecimal("4546.09"));

    override val category = ConverterCategory.Volume
}

fun convertVolume(value: BigDecimal, source: VolumeUnit, target: VolumeUnit): BigDecimal =
    value.multiply(source.millilitres).divide(target.millilitres, MathContext.DECIMAL128)
