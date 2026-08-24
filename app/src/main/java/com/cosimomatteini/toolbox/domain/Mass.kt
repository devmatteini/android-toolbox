package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

enum class MassUnit(override val symbol: String, val grams: BigDecimal) : ConverterUnit {
    Milligram("mg", BigDecimal("0.001")),
    Gram("g", BigDecimal.ONE),
    Kilogram("kg", BigDecimal("1000")),
    Ounce("oz", BigDecimal("28.349523125")),
    Pound("lb", BigDecimal("453.59237")),
    Stone("st", BigDecimal("6350.29318"));

    override val category = ConverterCategory.Mass
}

fun convertMass(value: BigDecimal, source: MassUnit, target: MassUnit): BigDecimal =
    value.multiply(source.grams).divide(target.grams, MathContext.DECIMAL128)
