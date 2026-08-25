package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

data class CurrencyUnit(val code: String, val eurRate: BigDecimal) : ConverterUnit {
    override val category = ConverterCategory.Currency
    override val symbol = code
}

fun convertCurrency(value: BigDecimal, source: CurrencyUnit, target: CurrencyUnit): BigDecimal =
    value.multiply(target.eurRate).divide(source.eurRate, MathContext.DECIMAL128)
