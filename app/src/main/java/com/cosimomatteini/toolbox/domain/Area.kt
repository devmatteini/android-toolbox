package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

enum class AreaUnit(override val symbol: String, val squareMeters: BigDecimal) : ConverterUnit {
    SquareCentimeter("cm²", BigDecimal("0.0001")),
    SquareMeter("m²", BigDecimal.ONE),
    SquareKilometer("km²", BigDecimal("1000000")),
    Hectare("ha", BigDecimal("10000")),
    SquareInch("in²", BigDecimal("0.00064516")),
    SquareFoot("ft²", BigDecimal("0.09290304")),
    SquareYard("yd²", BigDecimal("0.83612736")),
    SquareMile("mi²", BigDecimal("2589988.110336")),
    Acre("ac", BigDecimal("4046.8564224"));

    override val category = ConverterCategory.Area
}

fun convertArea(value: BigDecimal, source: AreaUnit, target: AreaUnit): BigDecimal =
    value.multiply(source.squareMeters).divide(target.squareMeters, MathContext.DECIMAL128)
